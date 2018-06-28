package uk.gov.justice.services.example.cakeshop.it;

import static com.google.common.io.Resources.getResource;
import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.IOUtils.contentEquals;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_QUERY_URI;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopUris.RECIPES_RESOURCE_URI;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.isStatus;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.example.cakeshop.it.helpers.CakeShopRepositoryManager;
import uk.gov.justice.services.example.cakeshop.it.helpers.CommandSender;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFactory;
import uk.gov.justice.services.example.cakeshop.it.helpers.EventFinder;
import uk.gov.justice.services.example.cakeshop.it.helpers.Querier;
import uk.gov.justice.services.example.cakeshop.it.helpers.RestEasyClientFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CakeShopFileServiceIT {

    private static final CakeShopRepositoryManager CAKE_SHOP_REPOSITORY_MANAGER = new CakeShopRepositoryManager();

    private final EventFinder eventFinder = new EventFinder(CAKE_SHOP_REPOSITORY_MANAGER);

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
        commandSender = new CommandSender(client, new EventFactory());
    }

    @After
    public void cleanup() throws Exception {
        client.close();
    }

    @Test
    public void shouldReturnAcceptedStatusAndCreatEventWhenPostingPhotographToMultipartEndpoint() throws Exception {

        final String recipeId = "163af847-effb-46a9-96bc-32a0f7526f22";
        final String fieldName = "photoId";
        final String filename = "croydon.jpg";
        final File file = new File(getResource(filename).getFile());

        commandSender.addRecipe(recipeId, "Cheesy cheese cake");

        await().until(() -> querier.queryForRecipe(recipeId).httpCode() == OK.getStatusCode());

        final HttpEntity httpEntity = MultipartEntityBuilder.create()
                .setMode(BROWSER_COMPATIBLE)
                .addBinaryBody(fieldName, file, APPLICATION_OCTET_STREAM, filename)
                .build();

        final HttpPost request = new HttpPost(RECIPES_RESOURCE_URI + recipeId + "/photograph");
        request.setEntity(httpEntity);

        final HttpResponse response = HttpClients.createDefault().execute(request);

        assertThat(response.getStatusLine().getStatusCode(), isStatus(ACCEPTED));

        await().until(() -> eventFinder.eventsWithPayloadContaining(recipeId).size() == 2);

        final Event event = eventFinder.eventsWithPayloadContaining(recipeId).get(1);
        assertThat(event.getName(), is("example.recipe-photograph-added"));
        with(event.getMetadata())
                .assertEquals("stream.id", recipeId)
                .assertEquals("stream.version", 2);
        with(event.getPayload())
                .assertThat("$.recipeId", equalTo(recipeId))
                .assertThat("$.photoId", notNullValue());
    }

    @Test
    public void shouldRetrieveRecipePhotograph() throws Exception {
        final String recipeId = "163af847-effb-46a9-96bc-32a0f7526f24";
        commandSender.addRecipe(recipeId, "Easy Muffin");
        await().until(() -> querier.queryForRecipe(recipeId).httpCode() == OK.getStatusCode());

        assertThat(photographFor(recipeId).get().getStatus(), isStatus(NOT_FOUND));

        final String filename = "croydon.jpg";
        appendFileToTheRecipe(recipeId, filename);

        await().until(() -> photographFor(recipeId).get().getStatus() == OK.getStatusCode());

        final InputStream returnedStream = photographFor(recipeId).get(InputStream.class);

        assertThat(contentEquals(returnedStream, fileStreamOf(filename)), is(true));

    }

    private InputStream fileStreamOf(final String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }

    private Invocation.Builder photographFor(final String recipeId) {
        return client.target(format("%s%s/photograph", RECIPES_RESOURCE_QUERY_URI, recipeId))
                .request()
                .accept(APPLICATION_OCTET_STREAM_TYPE);
    }

    private void appendFileToTheRecipe(final String recipeId, final String filename) throws IOException {
        final File file = new File(getResource(filename).getFile());


        final HttpEntity httpEntity = MultipartEntityBuilder.create()
                .setMode(BROWSER_COMPATIBLE)
                .addBinaryBody("photoId", file, APPLICATION_OCTET_STREAM, filename)
                .build();

        final HttpPost request = new HttpPost(RECIPES_RESOURCE_URI + recipeId + "/photograph");
        request.setEntity(httpEntity);

        HttpClients.createDefault().execute(request);
    }
}
