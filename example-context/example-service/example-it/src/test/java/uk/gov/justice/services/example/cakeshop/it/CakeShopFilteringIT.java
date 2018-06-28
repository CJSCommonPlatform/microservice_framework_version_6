package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.emptyCollection;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;

import uk.gov.justice.services.example.cakeshop.it.helpers.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandSender;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import javax.ws.rs.client.Client;

import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CakeShopFilteringIT {

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
    public void shouldFilterRecipesUsingPageSize() {
        //adding 2 recipes
        final String recipeId = "263af847-effb-46a9-96bc-32a0f7526e44";
        commandSender.addRecipe(recipeId, "Absolutely cheesy cheese cake");

        final String recipeId2 = "263af847-effb-46a9-96bc-32a0f7526e55";
        commandSender.addRecipe(recipeId2, "Chocolate muffin");

        await().until(() -> querier.recipesQueryResult().body().contains(recipeId));

        final ApiResponse response = querier.recipesQueryResult(singletonList(new BasicNameValuePair("pagesize", "1")));
        assertThat(response.httpCode(), is(OK.getStatusCode()));

        with(response.body())
                .assertThat("$.recipes[?(@.id=='" + recipeId2 + "')]", emptyCollection())
                .assertThat("$.recipes[?(@.id=='" + recipeId + "')].name", hasItem("Absolutely cheesy cheese cake"));
    }

    @Test
    public void shouldFilterGlutenFreeRecipes() {
        //adding 2 recipes
        final String recipeId = "163af847-effb-46a9-96bc-32a0f7526e66";
        client.target(RECIPES_RESOURCE_URI + recipeId).request()
                .post(eventFactory.recipeEntity("Muffin", false));

        final String recipeId2 = "163af847-effb-46a9-96bc-32a0f7526e77";
        client.target(RECIPES_RESOURCE_URI + recipeId2).request()
                .post(eventFactory.recipeEntity("Oat cake", true));

        await().until(() -> querier.recipesQueryResult().body().contains(recipeId2));

        final ApiResponse response = querier.recipesQueryResult(asList(
                new BasicNameValuePair("pagesize", "30"),
                new BasicNameValuePair("glutenFree", "true")));

        assertThat(response.httpCode(), is(OK.getStatusCode()));

        with(response.body())
                .assertThat("$.recipes[?(@.id=='" + recipeId + "')]", emptyCollection())
                .assertThat("$.recipes[?(@.id=='" + recipeId2 + "')].name", hasItem("Oat cake"));
    }
}
