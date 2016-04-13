package uk.gov.justice.api.resource;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static org.apache.cxf.jaxrs.client.WebClient.create;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

import uk.gov.justice.api.QueryApiRestExampleApplication;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapters.test.utils.dispatcher.AsynchronousRecordingDispatcher;
import uk.gov.justice.services.adapters.test.utils.dispatcher.SynchronousRecordingDispatcher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Properties;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Integration tests for the generated JAX-RS classes.
 */
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class DefaultUsersUserIdResourceIT {

    private static final String CREATE_USER_MEDIA_TYPE = "application/vnd.people.command.create-user+json";
    private static final String UPDATE_USER_MEDIA_TYPE = "application/vnd.people.command.update-user+json";
    private static int port = -1;
    private static String BASE_URI;

    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/query/api/rest/example";

    private static final String JSON = "{\"userUrn\" : \"test\"}";

    @Inject
    AsynchronousRecordingDispatcher asyncDispatcher;

    @Inject
    SynchronousRecordingDispatcher syncDispatcher;


    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URI = String.format(BASE_URI_PATTERN, port);
    }

    @Before
    public void before() {

    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            RestProcessor.class,
            RestEnvelopeBuilderFactory.class,
            AsynchronousRecordingDispatcher.class,
            SynchronousRecordingDispatcher.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-adapter-generator")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", QueryApiRestExampleApplication.class.getName());
    }

    @Test
    public void shouldReturn202CreatingUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, CREATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(202));
    }

    @Test
    public void shouldDispatchCreateUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/567-8910")
                .post(entity("{\"userName\" : \"John Smith\"}", CREATE_USER_MEDIA_TYPE));

        JsonEnvelope jsonEnvelope = asyncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "567-8910");
        assertThat(jsonEnvelope.metadata().name(), is("people.command.create-user"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("567-8910"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userName"), is("John Smith"));

    }


    @Test
    public void shouldReturn202UpdatingUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, UPDATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(202));
    }

    @Test
    public void shouldDispatchUpdateUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/4444-9876")
                .post(entity("{\"userName\" : \"Peggy Brown\"}", UPDATE_USER_MEDIA_TYPE));

        JsonEnvelope jsonEnvelope = asyncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(jsonEnvelope.metadata().name(), is("people.command.update-user"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userName"), is("Peggy Brown"));

    }

    @Test
    public void shouldDispatchGetUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();
        JsonEnvelope jsonEnvelope = syncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(jsonEnvelope.metadata().name(), is("people.query.get-user"));

    }

    @Test
    public void shouldDispatchGetUserCommandWithOtherMediaType() throws Exception {
        syncDispatcher.setupResponse("userId", "4444-5555", envelopeFrom(null, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.get-user2+json")
                .get();

        assertThat(response.getStatus(), is(200));
        JsonEnvelope jsonEnvelope = syncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(jsonEnvelope.metadata().name(), is("people.query.get-user2"));

    }

    @Test
    public void shouldReturn406ifQueryTypeNotRecognised() throws Exception {

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.unknown+json")
                .get();

        assertThat(response.getStatus(), is(406));

    }


    @Test
    public void shouldReturnUserDataReturnedByDispatcher() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(null, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();

        assertThat(response.getStatus(), is(200));
        String responseBody = response.readEntity(String.class);
        with(responseBody)
                .assertThat("userName", equalTo("userName"));

    }

    @Test
    public void shouldReturnResponseWithContentType() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(null, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();

        assertThat(response.getMediaType().toString(), is("application/vnd.people.query.get-user+json"));
    }

    @Test
    public void shouldReturnResponseWithSecondContentType() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(null, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user2+json")
                .get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.getMediaType().toString(), is("application/vnd.people.query.get-user2+json"));
    }

}
