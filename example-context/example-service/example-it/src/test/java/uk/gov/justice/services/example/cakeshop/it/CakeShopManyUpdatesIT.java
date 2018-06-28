package uk.gov.justice.services.example.cakeshop.it;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;

import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.JmsBootstrapper;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;
import uk.gov.justice.services.test.utils.core.messaging.Poller;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.ws.rs.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CakeShopManyUpdatesIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();
    private static final String COMMAND_HANDLER_QUEUE = "example.handler.command";
    private static final String DEAD_LETTER_QUEUE = "DLQ";

    private final JmsBootstrapper jmsBootstrapper = new JmsBootstrapper();
    private final EventFactory eventFactory = new EventFactory();
    private Querier querier;

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
        querier = new Querier(client);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldSucessfullyProcessManyUpdatesToSameRecipeId() throws Exception {
        jmsBootstrapper.clearDeadLetterQueue();

        final String recipeId = randomUUID().toString();
        final String recipeName = "Original Cheese Cake";

        client.target(RECIPES_RESOURCE_URI + recipeId)
                .request()
                .post(eventFactory.recipeEntity(recipeName));

        new Poller().pollUntilFound(() -> {
            if (querier.queryForRecipe(recipeId).httpCode() == OK.getStatusCode()) {
                return of(true);
            }

            return empty();
        });

        // Do many renames
        int updateCount = 10;
        for (int i = 0; i < updateCount; i++) {
            //random generator string
            client.target(RECIPES_RESOURCE_URI + recipeId)
                    .request()
                    .put(eventFactory.renameRecipeEntity("New Name"));
        }

        try (final Session jmsSession = jmsBootstrapper.jmsSession()) {

            final QueueBrowser queueBrowser = jmsBootstrapper.queueBrowserOf(COMMAND_HANDLER_QUEUE, jmsSession);

            new Poller().pollUntilNotFound(() -> {
                try {
                    if (queueBrowser.getEnumeration().hasMoreElements()) {
                        return of(true);
                    }
                } catch (JMSException e) {
                    System.out.println("Browsing Queue failed");
                    throw new RuntimeException(e);
                }

                return empty();
            });

            final MessageConsumer dlqConsumer = jmsBootstrapper.queueConsumerOf(DEAD_LETTER_QUEUE, jmsSession);
            final Message message = dlqConsumer.receiveNoWait();

            assertNull("Dead letter queue is not empty, found message: ", message);
        }

        client.target(RECIPES_RESOURCE_URI + recipeId).request()
                .put(eventFactory.renameRecipeEntity("Final Name"));

        new Poller().pollUntilFound(() -> {
            if (querier.queryForRecipe(recipeId).body().contains("Final Name")) {
                return of(true);
            }

            return empty();
        });
    }
}
