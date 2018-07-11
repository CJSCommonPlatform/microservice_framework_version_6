package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.ADD_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.QUERY_RECIPES_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_QUERY_URI;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.isStatus;

import uk.gov.justice.services.example.cakeshop.it.helpers.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandSender;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CakeShopFailuresIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final EventFactory eventFactory = new EventFactory();

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
    public void shouldReturnBadRequestIfJsonFailsValidation() throws Exception {
        final String recipeId = randomUUID().toString();
        final Response response = client.target(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity("{}", ADD_RECIPE_MEDIA_TYPE));
        assertThat(response.getStatus(), isStatus(BAD_REQUEST));
    }

    @Test
    public void shouldReturnBadRequestIfRequestParametersMissing() throws Exception {
        final Response response = client.target(RECIPES_RESOURCE_QUERY_URI).request().accept(QUERY_RECIPES_MEDIA_TYPE).get();
        assertThat(response.getStatus(), isStatus(BAD_REQUEST));
    }

    @Test
    public void shouldReturnBadRequestIfMediaTypeIsWrong() throws Exception {
        final Response response = client.target(RECIPES_RESOURCE_QUERY_URI).request().accept("*/*").get();
        assertThat(response.getStatus(), isStatus(BAD_REQUEST));
    }

    @Test
    public void shouldReturnNotFoundIfRecipeDoesNotExist() {
        final UUID recipeId = randomUUID();
        final ApiResponse response = querier.queryForRecipe(recipeId.toString());
        assertThat(response.httpCode(), isStatus(NOT_FOUND));
    }

    @Test
    public void shouldReturnBadRequestOnIncorrectNumericParamType() {
        final ApiResponse response = querier.recipesQueryResult(singletonList(
                new BasicNameValuePair("pagesize", "invalid")));

        assertThat(response.httpCode(), isStatus(BAD_REQUEST));
    }

    @Test
    public void shouldReturnBadRequestOnIncorrectBooleanParamType() {
        final ApiResponse response = querier.recipesQueryResult(asList(
                new BasicNameValuePair("pagesize", "30"),
                new BasicNameValuePair("glutenFree", "invalid")));

        assertThat(response.httpCode(), isStatus(BAD_REQUEST));
    }

    @Test
    public void shouldNotPersistRecipeWhenIngredientIfDatabaseInsertFails() throws Exception {
        final String recipeId = randomUUID().toString();

        client.target(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(
                        createObjectBuilder()
                                .add("name", "Transaction Failure Recipe Rollback Cake")
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "ingredient-with-long-name-to-exceed-database-column-length")
                                                .add("quantity", 1)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));

        Thread.sleep(500);

        assertThat(querier.queryForRecipe(recipeId).httpCode(), isStatus(NOT_FOUND));
    }

    @Test
    public void shouldRecoverAfterException() throws Exception {

        //The ExceptionThrowingInterceptor, in the event listener component, throws an exception on "Exceptional cake"
        //This triggers 4 exceptions to exhaust the connection pool if there's a connection leak
        for (int i = 0; i < 2; i++) {
            client.target(RECIPES_RESOURCE_URI + randomUUID())
                    .request()
                    .post(eventFactory.recipeEntity("Exceptional cake"));
        }
        final String recipeId = randomUUID().toString();
        final String recipeName = "Non exceptional cake";
        commandSender.addRecipe(recipeId, recipeName);

        await().until(() -> querier.queryForRecipe(recipeId).httpCode() == OK.getStatusCode());

        final ApiResponse response = querier.queryForRecipe(recipeId);

        with(response.body())
                .assertThat("$.id", equalTo(recipeId))
                .assertThat("$.name", equalTo(recipeName));
    }
}
