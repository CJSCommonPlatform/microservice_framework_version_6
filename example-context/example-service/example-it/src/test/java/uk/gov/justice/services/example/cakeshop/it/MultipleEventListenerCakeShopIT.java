package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.isStatus;

import uk.gov.justice.services.example.cakeshop.it.helpers.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandSender;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.JmsBootstrapper;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;

import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultipleEventListenerCakeShopIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final EventFactory eventFactory = new EventFactory();
    private final JmsBootstrapper jmsBootstrapper = new JmsBootstrapper();

    private Client client;
    private Querier querier;
    private CommandSender commandSender;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
        querier = new Querier(client);
        commandSender = new CommandSender(client, eventFactory);
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldReturnRecipeFromOtherEventListener() throws Exception {
        //adding 1 recipe as normal
        final UUID recipeId = randomUUID();
        commandSender.addRecipe(recipeId.toString(), "Cheesy cheese cake");

        //adding 1 recipe as other event
        final UUID recipeId2 = randomUUID();

        try (final Session jmsSession = jmsBootstrapper.jmsSession()) {
            final Topic topic = jmsSession.createTopic("other.event");

            try (final MessageProducer producer = jmsSession.createProducer(topic);) {

                final JsonObject jsonObject = createObjectBuilder()
                        .add("recipeId", recipeId2.toString())
                        .add("name", "Chocolate muffin")
                        .add("glutenFree", true)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "someIngredient")
                                        .add("quantity", 1)
                                ).build()
                        ).build();

                final JsonEnvelope jsonEnvelope = envelopeFrom(
                        metadataBuilder()
                                .withId(randomUUID())
                                .withName("other.recipe-added")
                                .withStreamId(recipeId2)
                                .withVersion(1L)
                                .build(),
                        jsonObject);

                @SuppressWarnings("deprecation") final String json = jsonEnvelope.toDebugStringPrettyPrint();
                final TextMessage message = jmsSession.createTextMessage();

                message.setText(json);
                message.setStringProperty("CPPNAME", "other.recipe-added");

                producer.send(message);
            }
        }

        await().until(() -> {
            final String responseBody = querier.recipesQueryResult(singletonList(new BasicNameValuePair("pagesize", "30"))).body();
            return responseBody.contains(recipeId.toString()) && responseBody.contains(recipeId2.toString());
        });

        final ApiResponse response = querier.recipesQueryResult();
        assertThat(response.httpCode(), isStatus(OK));

        with(response.body())
                .assertThat("$.recipes[?(@.id=='" + recipeId + "')].name", hasItem("Cheesy cheese cake"))
                .assertThat("$.recipes[?(@.id=='" + recipeId2 + "')].name", hasItem("Chocolate muffin"));
    }

    @Test
    public void shouldPublishEventToPublicTopic() throws Exception {
        try (final Session jmsSession = jmsBootstrapper.jmsSession()) {
            try (final MessageConsumer publicTopicConsumer = jmsBootstrapper.topicConsumerOf("public.event", jmsSession)) {

                final String recipeId = "163af847-effb-46a9-96bc-32a0f7526e13";
                client.target(RECIPES_RESOURCE_URI + recipeId).request()
                        .post(eventFactory.recipeEntity("Apple pie", false));

                final TextMessage message = (TextMessage) publicTopicConsumer.receive();
                with(message.getText())
                        .assertThat("$._metadata.name", equalTo("example.recipe-added"))
                        .assertThat("$._metadata.stream.id", equalTo(recipeId))
                        .assertThat("$.recipeId", equalTo(recipeId))
                        .assertThat("$.name", equalTo("Apple pie"));
            }
        }
    }


}
