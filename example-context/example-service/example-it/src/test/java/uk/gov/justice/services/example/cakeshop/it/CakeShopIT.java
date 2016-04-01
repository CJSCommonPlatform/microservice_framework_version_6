package uk.gov.justice.services.example.cakeshop.it;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLog;
import uk.gov.justice.services.example.cakeshop.it.util.StandaloneJdbcEventLogRepository;
import uk.gov.justice.services.example.cakeshop.it.util.TestProperties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.stream.Stream;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static javax.ws.rs.client.Entity.entity;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CakeShopIT {

    private static final int ACCEPTED = 202;
    private static final String RECIPES_RESOURCE_URI = "http://localhost:8080/example-command-api/command/api/rest/cakeshop/recipes/";
    private static final String CAKES_RESOURCE_URI = "http://localhost:8080/example-command-api/command/api/rest/cakeshop/cakes/";
    private static final String ADD_RECIPE_MEDIA_TYPE = "application/vnd.cakeshop.commands.add-recipe+json";
    private static final String MAKE_CAKE_MEDIA_TYPE = "application/vnd.cakeshop.commands.make-cake+json";
    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "db.url";
    private static final String DB_USER_NAME = "db.userName";
    private static final String DB_PASSWORD = "db.password";

    private static StandaloneJdbcEventLogRepository EVENT_LOG_REPOSITORY;

    private Client client;

    @Test
    public void shouldReturn202ResponseWhenAddingRecipe() throws Exception {

        String recipeId = "163af847-effb-46a9-96bc-32a0f7526f88";
        Response response = sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(addRecipeCommand(), ADD_RECIPE_MEDIA_TYPE));
        assertThat(response.getStatus(), is(ACCEPTED));
    }

    @Test
    public void shouldRegisterRecipeAddedEvent() {
        String recipeId = "163af847-effb-46a9-96bc-32a0f7526f99";
        sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(entity(addRecipeCommand(),
                        ADD_RECIPE_MEDIA_TYPE));

        await().until(() -> eventsWithPayloadContaining(recipeId).count() == 1);

        EventLog event = eventsWithPayloadContaining(recipeId).findFirst().get();
        assertThat(event.getName(), is("cakeshop.events.recipe-added"));
        String eventPayload = event.getPayload();
        with(eventPayload)
                .assertThat("$.recipeId", equalTo(recipeId))
                .assertThat("$.name", equalTo("Chocolate muffin in six easy steps"));

    }

    private String addRecipeCommand() {
        return jsonObject()
                .add("name", "Chocolate muffin in six easy steps")
                .add("ingredients", Json.createArrayBuilder().build())
                .build().toString();
    }

    @Test
    public void shouldReturn202ResponseWhenMakingCake() throws Exception {

        String cakeId = "163af847-effb-46a9-96bc-32a0f7526f77";
        Response response = sendTo(CAKES_RESOURCE_URI + cakeId).request()
                .post(entity("{}", MAKE_CAKE_MEDIA_TYPE));
        assertThat(response.getStatus(), is(ACCEPTED));

    }

    private JsonObjectBuilder jsonObject() {
        return Json.createObjectBuilder();
    }

    private Stream<EventLog> eventsWithPayloadContaining(String string) {
        return EVENT_LOG_REPOSITORY.findAll().filter(e -> e.getPayload().contains(string));
    }

    private WebTarget sendTo(String url) {
        return client.target(url);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        DataSource dataSource = initDatabase();
        EVENT_LOG_REPOSITORY = new StandaloneJdbcEventLogRepository(dataSource);

    }

    @Before
    public void before() throws Exception {
        client = new ResteasyClientBuilder().build();

    }

    @After
    public void cleanup() throws Exception {
        client.close();

    }

    private static DataSource initDatabase() throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(H2_DRIVER);
        TestProperties properties = TestProperties.getInstance();
        dataSource.setUrl(properties.value(DB_URL));
        dataSource.setUsername(properties.value(DB_USER_NAME));
        dataSource.setPassword(properties.value(DB_PASSWORD));

        Liquibase liquibase = new Liquibase(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
        return dataSource;
    }

}
