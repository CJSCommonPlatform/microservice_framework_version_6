package uk.gov.justice.api.resource;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.cxf.jaxrs.client.WebClient.create;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.api.QueryApiRestExampleApplication;
import uk.gov.justice.api.mapper.DefaultUsersResourceActionMapper;
import uk.gov.justice.api.mapper.DefaultUsersUserIdResourceActionMapper;
import uk.gov.justice.services.adapter.rest.application.CommonProviders;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.interceptor.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.adapter.rest.processor.RestProcessorProducer;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.generators.test.utils.interceptor.RecordingInterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.math.BigDecimal;
import java.util.Properties;

import javax.inject.Inject;
import javax.json.JsonObject;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class DefaultUsersUserIdResourceIT {

    private static final String CREATE_USER_MEDIA_TYPE = "application/vnd.people.user+json";
    private static final String UPDATE_USER_MEDIA_TYPE = "application/vnd.people.modified-user+json";
    private static final String LINK_USER_MEDIA_TYPE = "application/vnd.people.link-user+json";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/query/api/rest/example";
    private static final String JSON = "{\"userUrn\" : \"test\"}";
    private static int port = -1;
    private static String BASE_URI;

    @Inject
    RecordingInterceptorChainProcessor interceptorChainProcessor;

    @Inject
    CommonProviders commonProviders;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URI = String.format(BASE_URI_PATTERN, port);
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
            RestProcessorProducer.class,
            RestEnvelopeBuilderFactory.class,
            RecordingInterceptorChainProcessor.class,
            ObjectMapperProducer.class,
            JsonObjectEnvelopeConverter.class,
            CommonProviders.class,
            DummyCommonProviders.class,
            BadRequestExceptionMapper.class,
            JsonSchemaValidationInterceptor.class,
            JsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultUsersUserIdResourceActionMapper.class,
            DefaultUsersResourceActionMapper.class,
            LoggerProducer.class
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

        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldDispatchCreateUserCommand() throws Exception {
        create(BASE_URI)
                .path("/users/567-8910")
                .post(entity("{\"userUrn\" : \"1234\"}", CREATE_USER_MEDIA_TYPE));

        JsonEnvelope jsonEnvelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "567-8910");
        assertThat(jsonEnvelope.metadata().name(), is("people.create-user"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("567-8910"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userUrn"), is("1234"));
    }

    @Test
    public void shouldReturn400ForJsonNotAdheringToSchema() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity("{\"blah\" : \"1234\"}", CREATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, UPDATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn200ResponseContainingUserDataReturnedByDispatcher() {
        interceptorChainProcessor.setupResponse("userId", "4444-5556",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());


        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.user+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        String responseBody = response.readEntity(String.class);
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    @Test
    public void shouldDispatchUpdateUserCommand() throws Exception {
        create(BASE_URI)
                .path("/users/4444-9876")
                .post(entity("{\"userUrn\" : \"5678\"}", UPDATE_USER_MEDIA_TYPE));

        JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.update-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(envelope.payloadAsJsonObject().getString("userUrn"), is("5678"));
    }

    @Test
    public void shouldDispatchLinkUserCommandWhichHasNoBody() throws Exception {
        create(BASE_URI)
                .path("/users/4444-9877")
                .post(entity("", LINK_USER_MEDIA_TYPE));

        JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9877");
        assertThat(envelope.metadata().name(), is("people.link-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9877"));
    }

    @Test
    public void shouldDispatchGetUserCommand() throws Exception {
        create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.user+json")
                .get();
        JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user"));
    }

    @Test
    public void shouldDispatchGetUserCommandWithOtherMediaType() throws Exception {
        interceptorChainProcessor.setupResponse("userId", "4444-5555", envelope().with(metadataWithDefaults()).build());

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.user-summary+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user-summary"));
    }

    @Test
    public void shouldReturn406ifQueryTypeNotRecognised() throws Exception {

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.unknown+json")
                .get();

        assertThat(response.getStatus(), is(NOT_ACCEPTABLE.getStatusCode()));

    }

    @Test
    public void shouldReturnResponseWithContentType() {
        interceptorChainProcessor.setupResponse("userId", "4444-5556", envelope().with(metadataWithDefaults()).build());

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.user+json")
                .get();
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is("application/vnd.people.user+json"));
    }

    @Test
    public void shouldReturnResponseWithSecondContentType() {
        interceptorChainProcessor.setupResponse("userId", "4444-5556", envelope().with(metadataWithDefaults()).build());

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.user-summary+json")
                .get();
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is("application/vnd.people.user-summary+json"));
    }

    @Test
    public void shouldDispatchUsersQueryWithQueryParams() throws Exception {
        interceptorChainProcessor.setupResponse("lastname", "Smith", envelope().with(metadataWithDefaults()).build());

        Response response = create(BASE_URI)
                .path("/users")
                .query("lastname", "Smith")
                .query("firstname", "John")
                .query("height", 175.5)
                .query("married", true)
                .query("age", 34)
                .header("Accept", "application/vnd.people.users+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        JsonEnvelope jsonEnvelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("lastname", "Smith");
        assertThat(jsonEnvelope.metadata().name(), is("people.search-users"));

        final JsonObject payload = (JsonObject) jsonEnvelope.payload();
        assertThat(payload.getString("lastname"), is("Smith"));
        assertThat(payload.getString("firstname"), is("John"));
        assertThat(payload.getInt("age"), is(34));
        assertThat(payload.getJsonNumber("height").bigDecimalValue(), is(BigDecimal.valueOf(175.5)));
        assertThat(payload.getBoolean("married"), is(true));
    }

    @Test
    public void shouldReturn400IfRequiredQueryParamIsNotProvided() throws Exception {

        Response response = create(BASE_URI)
                .path("/users")
                .query("firstname", "firstname")
                .header("Accept", "application/vnd.people.users+json")
                .get();

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn200WhenOptionalParameterIsNotProvided() throws Exception {
        interceptorChainProcessor.setupResponse("lastname", "lastname", envelope().with(metadataWithDefaults()).build());

        Response response = create(BASE_URI)
                .path("/users")
                .query("lastname", "lastname")
                .header("Accept", "application/vnd.people.users+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
    }

    @Test
    public void shouldAllowDependencyInjectionToOverrideCommonProviders() {
        assertThat(commonProviders.getClass() == DummyCommonProviders.class, is(true));
    }
}
