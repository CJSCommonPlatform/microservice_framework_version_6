package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.ADD_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.REMOVE_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_QUERY_URI;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFinder;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;
import uk.gov.justice.services.test.utils.core.http.HttpResponsePoller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CakeShopConcurrencyIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final HttpResponsePoller httpResponsePoller = new HttpResponsePoller();
    private final EventFinder eventFinder = new EventFinder(CAKE_SHOP_REPOSITORY_MANAGER);

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CAKE_SHOP_REPOSITORY_MANAGER.initialise();
    }

    @Before
    public void before() throws Exception {
        client = new RestEasyClientFactory().createResteasyClient();
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public final void concurrentAddAndRemoveRecipe() throws InterruptedException {
        final ExecutorService exec = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            exec.execute(this::shouldRegisterRecipeRemovedEvent);
        }
        exec.shutdown();
        exec.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    public void shouldRegisterRecipeRemovedEvent() {


        final String recipeId = randomUUID().toString();
        client.target(RECIPES_RESOURCE_URI + recipeId)
                .request()
                .post(entity(
                        createObjectBuilder()
                                .add("name", "Vanilla cake")
                                .add("glutenFree", false)
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "vanilla")
                                                .add("quantity", 2)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));

        await().until(() -> eventFinder.eventsWithPayloadContaining(recipeId).size() == 1);

        final Event event = eventFinder.eventsWithPayloadContaining(recipeId).get(0);
        assertThat(event.getName(), is("example.recipe-added"));
        with(event.getMetadata())
                .assertEquals("stream.id", recipeId)
                .assertEquals("stream.version", 1);
        final String eventPayload = event.getPayload();
        with(eventPayload)
                .assertThat("$.recipeId", equalTo(recipeId))
                .assertThat("$.name", equalTo("Vanilla cake"))
                .assertThat("$.glutenFree", equalTo(false))
                .assertThat("$.ingredients[0].name", equalTo("vanilla"))
                .assertThat("$.ingredients[0].quantity", equalTo(2));

        final String foundResponse = httpResponsePoller.pollUntilFound(RECIPES_RESOURCE_QUERY_URI + recipeId, QUERY_RECIPE_MEDIA_TYPE);
        assertThat(foundResponse, notNullValue());

        client.target(RECIPES_RESOURCE_URI + recipeId)
                .request()
                .post(entity(createObjectBuilder()
                        .add("recipeId", recipeId)
                        .build()
                        .toString(), REMOVE_RECIPE_MEDIA_TYPE)
                );

        await().until(() -> eventFinder.eventsWithPayloadContaining(recipeId).size() == 2);

        final String notFoundResponse = httpResponsePoller.pollUntilNotFound(RECIPES_RESOURCE_QUERY_URI + recipeId, QUERY_RECIPE_MEDIA_TYPE);
        assertThat(notFoundResponse, notNullValue());
    }
}
