package uk.gov.justice.services.example.cakeshop.it;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static org.junit.Assert.assertNull;

import uk.gov.justice.services.example.cakeshop.it.util.ApiResponse;
import uk.gov.justice.services.example.cakeshop.it.util.TestProperties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.json.JsonObjectBuilder;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * To run the CakeShopPostgresIT tests do the following steps:
 *
 * 1. In the example-it/pom.xml enable all 'To be used when running CakeShopPostgresIT' commands.
 *
 * 2. In the example-it/pom.xml disable all 'To be used when running CakeShopIT' commands.
 *
 * 3. Disable all tests in CakeShopIT.
 *
 * 4. Enable all tests in CakeShopPostgresIT
 *
 * Requires a running postgres database on port 5432 with exampleeventstore and exampleviewstore
 * databases.
 */
@Ignore("Requires Postgres database")
public class CakeShopPostgresIT {

    private static final int OK = 200;

    //Postgres driver used when testing against postgres setup
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    private static final String RECIPES_RESOURCE_URI = "http://localhost:8080/example-command-api/command/api/rest/cakeshop/recipes/";
    private static final String RECIPES_RESOURCE_QUERY_URI = "http://localhost:8080/example-query-api/query/api/rest/cakeshop/recipes/";
    private static final String ADD_RECIPE_MEDIA_TYPE = "application/vnd.example.add-recipe+json";
    private static final String RENAME_RECIPE_MEDIA_TYPE = "application/vnd.example.rename-recipe+json";
    private static final String QUERY_RECIPE_MEDIA_TYPE = "application/vnd.example.recipe+json";

    private static final String JMS_USERNAME = "jmsuser";

    private static final String JMS_PASSWORD = "jms@user123";
    private static final String JMS_PORT = System.getProperty("random.jms.port");
    private static final String JMS_BROKER_URL = "tcp://localhost:" + JMS_PORT;

    private static final TestProperties TEST_PROPERTIES = new TestProperties("test-vagrant-postgres.properties");

    private static ActiveMQConnectionFactory JMS_CONNECTION_FACTORY;

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        JMS_CONNECTION_FACTORY = new ActiveMQConnectionFactory(JMS_BROKER_URL);
        Thread.sleep(300);
    }

    @Before
    public void before() throws Exception {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        cm.setMaxTotal(200); // Increase max total connection to 200
        cm.setDefaultMaxPerRoute(20); // Increase default max connection per route to 20
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient);
        client = new ResteasyClientBuilder().httpEngine(engine).build();
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldProcessMultipleUpdatedToSameRecipeId() throws Exception {
        clearDeadLetterQueue();

        final String recipeId = randomUUID().toString();
        final String recipeName = "Original Cheese Cake";

        sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                .post(recipeEntity(recipeName));

        await().until(() -> queryForRecipe(recipeId).httpCode() == OK);

        // Do many renames
        int updateCount = 500;
        for (int i = 0; i < updateCount; i++) {
            //random generator string
            sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                    .put(renameRecipeEntity("New Name"));
        }

        Thread.sleep(updateCount * 1 * 50);

        try (final Session jmsSession = jmsSession();) {
            final MessageConsumer dlqConsumer = queueConsumerOf(jmsSession, "DLQ");
            final Message message = dlqConsumer.receiveNoWait();

            assertNull("Dead letter queue is not empty, found message: ", message);

            sendTo(RECIPES_RESOURCE_URI + recipeId).request()
                    .put(renameRecipeEntity("Final Name"));

            await().until(() -> queryForRecipe(recipeId).body().contains("Final Name"));
        }
    }

    private void clearDeadLetterQueue() throws Exception {
        try (final Session jmsSession = jmsSession();) {
            final MessageConsumer dlqConsumer = queueConsumerOf(jmsSession, "DLQ");
            clear(dlqConsumer);
        }
    }

    private MessageConsumer queueConsumerOf(final Session session, final String queueName) throws JMSException {
        final Queue queue = session.createQueue(queueName);
        return session.createConsumer(queue);
    }

    private Session jmsSession() throws JMSException {
        final javax.jms.Connection connection = JMS_CONNECTION_FACTORY.createConnection(JMS_USERNAME, JMS_PASSWORD);
        connection.start();
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private static DataSource initEventStoreDb() throws Exception {
        return initDatabase("db.eventstore.url", "db.eventstore.userName",
                "db.eventstore.password", "liquibase/event-store-db-changelog.xml", "liquibase/snapshot-store-db-changelog.xml");
    }

    private ApiResponse queryForRecipe(final String recipeId) {
        final Response jaxrsResponse = sendTo(RECIPES_RESOURCE_QUERY_URI + recipeId).request().accept(QUERY_RECIPE_MEDIA_TYPE).get();
        return ApiResponse.from(jaxrsResponse);
    }

    private static DataSource initDatabase(final String dbUrlPropertyName,
                                           final String dbUserNamePropertyName,
                                           final String dbPasswordPropertyName,
                                           final String... liquibaseChangeLogXmls) throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(POSTGRES_DRIVER);

        dataSource.setUrl(TEST_PROPERTIES.value(dbUrlPropertyName));
        dataSource.setUsername(TEST_PROPERTIES.value(dbUserNamePropertyName));
        dataSource.setPassword(TEST_PROPERTIES.value(dbPasswordPropertyName));
        boolean dropped = false;
        final JdbcConnection jdbcConnection = new JdbcConnection(dataSource.getConnection());

        for (String liquibaseChangeLogXml : liquibaseChangeLogXmls) {
            Liquibase liquibase = new Liquibase(liquibaseChangeLogXml,
                    new ClassLoaderResourceAccessor(), jdbcConnection);
            if (!dropped) {
                liquibase.dropAll();
                dropped = true;
            }
            liquibase.update("");
        }
        return dataSource;
    }

    private JsonObjectBuilder jsonObject() {
        return createObjectBuilder();
    }

    private WebTarget sendTo(String url) {
        return client.target(url);
    }

    private Entity<String> renameRecipeEntity(final String recipeName) {
        return entity(
                jsonObject()
                        .add("name", recipeName)
                        .build().toString(),
                RENAME_RECIPE_MEDIA_TYPE);
    }

    private Entity<String> recipeEntity(final String recipeName) {
        return entity(
                jsonObject()
                        .add("name", recipeName)
                        .add("glutenFree", false)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "someIngredient")
                                        .add("quantity", 1)
                                ).build()
                        ).build().toString(),
                ADD_RECIPE_MEDIA_TYPE);
    }

    private void clear(MessageConsumer msgConsumer) throws JMSException {
        while (msgConsumer.receiveNoWait() != null) {
        }
    }
}
