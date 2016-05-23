package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.example.cakeshop.it.util.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.util.StandaloneJdbcEventLogRepository;
import uk.gov.justice.services.example.cakeshop.it.util.TestProperties;

import java.util.stream.Stream;

import javax.json.JsonObjectBuilder;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CakeShopIT {

    private static final int OK = 200;
    private static final int ACCEPTED = 202;
    private static final int NOT_FOUND = 404;
    private static final int BAD_REQUEST = 400;
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String RECIPES_RESOURCE_URI = "http://localhost:8080/example-command-api/command/api/rest/cakeshop/recipes/";
    private static final String CAKES_RESOURCE_URI = "http://localhost:8080/example-command-api/command/api/rest/cakeshop/cakes/";
    private static final String RECIPES_RESOURCE_QUERY_URI = "http://localhost:8080/example-query-api/query/api/rest/cakeshop/recipes/";
    private static final String ADD_RECIPE_MEDIA_TYPE = "application/vnd.cakeshop.command.add-recipe+json";
    private static final String MAKE_CAKE_MEDIA_TYPE = "application/vnd.cakeshop.command.make-cake+json";
    private static final String QUERY_RECIPE_MEDIA_TYPE = "application/vnd.cakeshop.query.recipe+json";
    private static final String QUERY_RECIPES_MEDIA_TYPE = "application/vnd.cakeshop.query.recipes+json";

    private static StandaloneJdbcEventLogRepository EVENT_LOG_REPOSITORY;

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        DataSource eventStoredataSource = initDatabase("db.eventstore.url", "db.eventstore.userName",
                "db.eventstore.password", "liquibase/event-store-db-changelog.xml");
        EVENT_LOG_REPOSITORY = new StandaloneJdbcEventLogRepository(eventStoredataSource);

        initDatabase("db.cakeshop.url", "db.cakeshop.userName", "db.cakeshop.password",
                "liquibase/view-store-db-changelog.xml");
    }


    @Test
    public void shouldReturn202ResponseWhenAddingRecipe() throws Exception {

        String recipeId = "163af847-effb-46a9-96bc-32a0f7526f88";
        Response response = sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(addRecipeCommand(), ADD_RECIPE_MEDIA_TYPE));
        assertThat(response.getStatus(), is(ACCEPTED));
    }

    @Test
    public void shouldReturn400ResponseWhenJsonNotAdheringToSchemaIsSent() throws Exception {

        String cakeId = "163af847-effb-46a9-96bc-32a0f7526f77";
        Response response = sendTo(CAKES_RESOURCE_URI + cakeId).request()
                .post(entity("{}", MAKE_CAKE_MEDIA_TYPE));
        assertThat(response.getStatus(), is(BAD_REQUEST));
    }

    @Test
    public void shouldRegisterRecipeAddedEvent() {
        String recipeId = "163af847-effb-46a9-96bc-32a0f7526f99";
        sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(
                        jsonObject()
                                .add("name", "Vanilla cake")
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "vanilla")
                                                .add("quantity", 2)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));

        await().until(() -> eventsWithPayloadContaining(recipeId).count() == 1);

        EventLog event = eventsWithPayloadContaining(recipeId).findFirst().get();
        assertThat(event.getName(), is("cakeshop.events.recipe-added"));
        String eventPayload = event.getPayload();
        with(eventPayload)
                .assertThat("$.recipeId", equalTo(recipeId))
                .assertThat("$.name", equalTo("Vanilla cake"))
                .assertThat("$.ingredients[0].name", equalTo("vanilla"))
                .assertThat("$.ingredients[0].quantity", equalTo(2));
    }

    @Test
    public void shouldReturn200WhenQueryingForRecipes() throws Exception {

        Response response = sendTo(RECIPES_RESOURCE_QUERY_URI + "?pagesize=10").request().accept(QUERY_RECIPES_MEDIA_TYPE).get();
        assertThat(response.getStatus(), is(OK));
    }


    @Test
    public void shouldReturn400WhenMandatoryQueryParamNotProvided() throws Exception {

        Response response = sendTo(RECIPES_RESOURCE_QUERY_URI).request().get();
        assertThat(response.getStatus(), is(BAD_REQUEST));
    }


    @Test
    public void shouldReturn404IfRecipeDoesNotExist() {
        ApiResponse response = queryForRecipe("163af847-effb-46a9-96bc-32a0f7526f00");
        assertThat(response.httpCode(), is(NOT_FOUND));

    }


    @Test
    public void shouldReturnRecipeOfGivenId() {
        String recipeId = "163af847-effb-46a9-96bc-32a0f7526f22";
        sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(
                        jsonObject()
                                .add("name", "Cheesy cheese cake")
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "cheese")
                                                .add("quantity", 1)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));


        await().until(() -> queryForRecipe(recipeId).httpCode() == OK);

        ApiResponse response = queryForRecipe(recipeId);

        with(response.body())
                .assertThat("$.id", equalTo(recipeId))
                .assertThat("$.name", equalTo("Cheesy cheese cake"));

    }


    @Test
    public void shouldReturnRecipes() {

        //adding 2 recipes
        String recipeId = "263af847-effb-46a9-96bc-32a0f7526e44";
        sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(
                        jsonObject()
                                .add("name", "Cheesy cheese cake")
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "cheese")
                                                .add("quantity", 1)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));

        String recipeId2 = "263af847-effb-46a9-96bc-32a0f7526e55";
        sendTo(RECIPES_RESOURCE_URI + recipeId2).request()
                .post(entity(
                        jsonObject()
                                .add("name", "Chocolate muffin")
                                .add("ingredients", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("name", "chocolate")
                                                .add("quantity", 3)
                                        ).build()
                                ).build().toString(),
                        ADD_RECIPE_MEDIA_TYPE));


        await().until(() -> {
            String responseBody = queryForRecipes().body();
            return responseBody.contains(recipeId) && responseBody.contains(recipeId2);
        });

        ApiResponse response = queryForRecipes();
        assertThat(response.httpCode(), is(OK));

        with(response.body())
                .assertThat("$.recipes[?(@.id=='"+recipeId+"')].name", hasItem("Cheesy cheese cake"))
                .assertThat("$.recipes[?(@.id=='"+recipeId2+"')].name", hasItem("Chocolate muffin"));

    }

    @Test
    public void shouldReturn202ResponseWhenMakingCake() throws Exception {

        String cakeId = "163af847-effb-46a9-96bc-32a0f7526f11";
        Response response = sendTo(CAKES_RESOURCE_URI + cakeId).request()
                .post(entity(makeCakeCommand(), MAKE_CAKE_MEDIA_TYPE));
        assertThat(response.getStatus(), is(ACCEPTED));
    }



    private ApiResponse queryForRecipe(String recipeId) {
        Response jaxrsResponse = sendTo(RECIPES_RESOURCE_QUERY_URI + recipeId).request().accept(QUERY_RECIPE_MEDIA_TYPE).get();
        ApiResponse response = ApiResponse.from(jaxrsResponse);
        jaxrsResponse.close();
        return response;
    }

    private ApiResponse queryForRecipes() {
        Response jaxrRsResponse = sendTo(RECIPES_RESOURCE_QUERY_URI + "?pagesize=5").request().accept(QUERY_RECIPES_MEDIA_TYPE).get();
        ApiResponse apiResponse = ApiResponse.from(jaxrRsResponse);
        jaxrRsResponse.close();
        return apiResponse;
    }

    private String addRecipeCommand() {
        return jsonObject()
                .add("name", "Chocolate muffin in six easy steps")
                .add("ingredients", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("name", "chocolate")
                                .add("quantity", 1)
                        ).build())
                .build().toString();
    }

    private static DataSource initDatabase(String dbUrlPropertyName,
                                           String dbUserNamePropertyName,
                                           String dbPasswordPropertyName,
                                           String liquibaseEventStoreDbChangelogXml) throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(H2_DRIVER);
        TestProperties properties = TestProperties.getInstance();
        dataSource.setUrl(properties.value(dbUrlPropertyName));
        dataSource.setUsername(properties.value(dbUserNamePropertyName));
        dataSource.setPassword(properties.value(dbPasswordPropertyName));

        Liquibase liquibase = new Liquibase(liquibaseEventStoreDbChangelogXml,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
        return dataSource;
    }

    private String makeCakeCommand() {
        return jsonObject()
                .add("recipeId", "163af847-effb-46a9-96bc-32a0f7526f99")
                .build().toString();
    }


    private JsonObjectBuilder jsonObject() {
        return createObjectBuilder();
    }

    private Stream<EventLog> eventsWithPayloadContaining(String string) {
        return EVENT_LOG_REPOSITORY.findAll().filter(e -> e.getPayload().contains(string));
    }

    private WebTarget sendTo(String url) {
        return  client.target(url);
    }

    @Before
    public void before() throws Exception {
        client = new ResteasyClientBuilder().connectionPoolSize(3).build();

    }

    @After
    public void cleanup() throws Exception {
        client.close();

    }

}
