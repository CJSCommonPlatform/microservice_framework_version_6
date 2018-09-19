package uk.gov.justice.api.resource;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.api.QueryApiRestExampleApplication;
import uk.gov.justice.api.mapper.DefaultQueryApiUsersResourceActionMapper;
import uk.gov.justice.api.mapper.DefaultQueryApiUsersUserIdResourceActionMapper;
import uk.gov.justice.api.mapper.RestAdapterGeneratorMediaTypeToSchemaIdMapper;
import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.adapter.rest.application.CommonProviders;
import uk.gov.justice.services.adapter.rest.application.DefaultCommonProviders;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.filter.JsonValidatorRequestFilter;
import uk.gov.justice.services.adapter.rest.filter.LoggerRequestDataFilter;
import uk.gov.justice.services.adapter.rest.interceptor.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.adapter.rest.mapping.BasicActionMapperHelper;
import uk.gov.justice.services.adapter.rest.multipart.DefaultFileInputDetailsFactory;
import uk.gov.justice.services.adapter.rest.multipart.FileBasedInterceptorContextFactory;
import uk.gov.justice.services.adapter.rest.multipart.InputPartFileNameExtractor;
import uk.gov.justice.services.adapter.rest.parameter.ValidParameterCollectionBuilderFactory;
import uk.gov.justice.services.adapter.rest.processor.DefaultRestProcessor;
import uk.gov.justice.services.adapter.rest.processor.ResponseStrategyCache;
import uk.gov.justice.services.adapter.rest.processor.response.AcceptedStatusNoEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopeEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.OkStatusEnvelopePayloadEntityResponseStrategy;
import uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategyHelper;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.envelope.EnvelopeInspector;
import uk.gov.justice.services.core.envelope.MediaTypeProvider;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.json.BackwardsCompatibleJsonSchemaValidator;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMappingObserver;
import uk.gov.justice.services.core.mapping.DefaultMediaTypesMappingCache;
import uk.gov.justice.services.core.mapping.DefaultNameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCacheInitialiser;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.generators.test.utils.interceptor.RecordingInterceptorChainProcessor;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.DefaultHttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

    private static final String USER_MEDIA_TYPE = "application/vnd.people.user+json";
    private static final String UPDATE_USER_MEDIA_TYPE = "application/vnd.people.modified-user+json";
    private static final String LINK_USER_MEDIA_TYPE = "application/vnd.people.link-user+json";
    private static final String DELETE_USER_MEDIA_TYPE = "application/vnd.people.delete-user+json";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/query/api/rest/example";
    private static final String JSON = "{\"userUrn\" : \"test\"}";
    private static int port = -1;
    private static String BASE_URI;

    private CloseableHttpClient httpClient;

    @Inject
    RecordingInterceptorChainProcessor interceptorChainProcessor;

    @Inject
    CommonProviders commonProviders;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URI = String.format(BASE_URI_PATTERN, port);
    }

    @Before
    public void setup() {
        httpClient = HttpClients.createDefault();
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
            DefaultRestProcessor.class,
            OkStatusEnvelopeEntityResponseStrategy.class,
            OkStatusEnvelopePayloadEntityResponseStrategy.class,
            AcceptedStatusNoEntityResponseStrategy.class,
            ResponseStrategyHelper.class,
            RestEnvelopeBuilderFactory.class,
            RecordingInterceptorChainProcessor.class,
            ObjectMapperProducer.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultCommonProviders.class,
            DummyCommonProviders.class,
            BadRequestExceptionMapper.class,
            JsonValidatorRequestFilter.class,
            JsonSchemaValidationInterceptor.class,
            LoggerRequestDataFilter.class,
            TestServiceContextNameProvider.class,
            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultQueryApiUsersUserIdResourceActionMapper.class,
            DefaultQueryApiUsersResourceActionMapper.class,
            BasicActionMapperHelper.class,
            LoggerProducer.class,
            StringToJsonObjectConverter.class,
            FileBasedInterceptorContextFactory.class,
            InputPartFileNameExtractor.class,
            DefaultFileInputDetailsFactory.class,
            ResponseStrategyCache.class,
            ValidParameterCollectionBuilderFactory.class,
            DefaultTraceLogger.class,
            DefaultHttpTraceLoggerHelper.class,
            RestAdapterGeneratorMediaTypeToSchemaIdMapper.class,
            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            DefaultNameToMediaTypeConverter.class,

            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,
            MediaTypeToSchemaIdMapper.class,
            BeanInstantiater.class,
            MediaTypeToSchemaIdMapper.class,

            CatalogProducer.class,
            SchemaCatalogService.class,
            SchemaCatalogResolverProducer.class,

            DefaultMediaTypesMappingCache.class,
            ActionNameToMediaTypesMappingObserver.class,
            MediaTypeProvider.class,
            BackwardsCompatibleJsonSchemaValidator.class,
            EnvelopeInspector.class,
            DefaultJsonValidationLoggerHelper.class,

            MediaTypesMappingCacheInitialiser.class,
            SchemaIdMappingCacheInitialiser.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-adapter-generator")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", QueryApiRestExampleApplication.class.getName());
    }

    @Test
    public void shouldReturn202CreatingUser() throws Exception {
        final HttpResponse response = httpClient.execute(postRequestFor("/users/1234", JSON, USER_MEDIA_TYPE));
        assertThat(response.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldDispatchCreateUserCommand() throws Exception {
        httpClient.execute(postRequestFor("/users/567-8910", "{\"userUrn\" : \"1234\"}", USER_MEDIA_TYPE));

        final JsonEnvelope jsonEnvelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "567-8910");
        assertThat(jsonEnvelope.metadata().name(), is("people.create-user"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("567-8910"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userUrn"), is("1234"));
    }

    @Test
    public void shouldReturn400ForJsonNotAdheringToSchema() throws Exception {
        final HttpResponse response = httpClient.execute(postRequestFor("/users/1234", "{\"blah\" : \"1234\"}", USER_MEDIA_TYPE));
        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUserWithPost() throws Exception {
        final HttpResponse response = httpClient.execute(postRequestFor("/users/1234", JSON, UPDATE_USER_MEDIA_TYPE));
        assertThat(response.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUserWithPut() throws Exception {
        final HttpResponse response = httpClient.execute(putRequestFor("/users/1234", JSON, UPDATE_USER_MEDIA_TYPE));
        assertThat(response.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUserWithPatch() throws Exception {
        final HttpResponse httpResponse = httpClient.execute(patchRequestFor("/users/1234", JSON, UPDATE_USER_MEDIA_TYPE));
        assertThat(httpResponse.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUserWithDelete() throws Exception {
        final HttpResponse httpResponse = httpClient.execute(deleteRequestFor("/users/1234", DELETE_USER_MEDIA_TYPE));
        assertThat(httpResponse.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn200ResponseContainingUserDataReturnedByDispatcher() throws Exception {
        interceptorChainProcessor.setupResponse("userId", "4444-5556",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());

        final HttpResponse response = httpClient.execute(getRequestFor("/users/4444-5556", "application/vnd.people.user+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String responseBody = EntityUtils.toString(response.getEntity());
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    @Test
    public void shouldReturn200ResponseForSynchronousPOST() throws Exception {
        interceptorChainProcessor.setupResponse("userUrn", "test",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());

        final HttpResponse response = httpClient.execute(postRequestFor("/users", JSON, USER_MEDIA_TYPE, "application/vnd.people.user+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String responseBody = EntityUtils.toString(response.getEntity());
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    @Test
    public void shouldReturn200ResponseForSynchronousPUT() throws Exception {
        interceptorChainProcessor.setupResponse("userUrn", "test",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());

        final HttpResponse response = httpClient.execute(putRequestFor("/users", JSON, USER_MEDIA_TYPE, "application/vnd.people.user+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String responseBody = EntityUtils.toString(response.getEntity());
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    @Test
    public void shouldReturn200ResponseForSynchronousPATCH() throws Exception {
        interceptorChainProcessor.setupResponse("userUrn", "test",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());

        final HttpResponse response = httpClient.execute(patchRequestFor("/users", JSON, USER_MEDIA_TYPE, "application/vnd.people.user+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final String responseBody = EntityUtils.toString(response.getEntity());
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    @Test
    public void shouldDispatchPostUpdateUserCommand() throws Exception {
        httpClient.execute(postRequestFor("/users/4444-9876", "{\"userUrn\" : \"5678\"}", UPDATE_USER_MEDIA_TYPE));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.update-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(envelope.payloadAsJsonObject().getString("userUrn"), is("5678"));
    }

    @Test
    public void shouldDispatchPutUpdateUserCommand() throws Exception {
        httpClient.execute(putRequestFor("/users/4444-9876", "{\"userUrn\" : \"5678\"}", UPDATE_USER_MEDIA_TYPE));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.update-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(envelope.payloadAsJsonObject().getString("userUrn"), is("5678"));
    }

    @Test
    public void shouldDispatchPatchUpdateUserCommand() throws Exception {
        httpClient.execute(patchRequestFor("/users/4444-9876", "{\"userUrn\" : \"5678\"}", UPDATE_USER_MEDIA_TYPE));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.update-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(envelope.payloadAsJsonObject().getString("userUrn"), is("5678"));
    }

    @Test
    public void shouldDispatchDeleteUpdateUserCommand() throws Exception {
        httpClient.execute(deleteRequestFor("/users/4444-9876", DELETE_USER_MEDIA_TYPE));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.delete-user"));
    }

    @Test
    public void shouldDispatchLinkUserCommandWhichHasNoBody() throws Exception {
        httpClient.execute(postRequestFor("/users/4444-9877", "", LINK_USER_MEDIA_TYPE));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-9877");
        assertThat(envelope.metadata().name(), is("people.link-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9877"));
    }

    @Test
    public void shouldDispatchGetUserCommand() throws Exception {
        httpClient.execute(getRequestFor("/users/4444-5555", "application/vnd.people.user+json"));

        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user"));
    }

    @Test
    public void shouldDispatchGetUserCommandWithOtherMediaType() throws Exception {
        interceptorChainProcessor.setupResponse("userId", "4444-5555", envelope().with(metadataWithDefaults()).build());

        final HttpResponse response = httpClient.execute(getRequestFor("/users/4444-5555", "application/vnd.people.user-summary+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final JsonEnvelope envelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user-summary"));
    }

    @Test
    public void shouldReturn406ifQueryTypeNotRecognised() throws Exception {
        final HttpResponse response = httpClient.execute(getRequestFor("/users/4444-5555", "application/vnd.people.query.unknown+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(NOT_ACCEPTABLE.getStatusCode()));
    }

    @Test
    public void shouldReturnResponseWithContentType() throws Exception {
        interceptorChainProcessor.setupResponse("userId", "4444-5556", envelope().with(metadataWithDefaults()).build());

        final HttpResponse response = httpClient.execute(getRequestFor("/users/4444-5556", "application/vnd.people.user+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        assertThat(response.getEntity().getContentType().getValue(), is("application/vnd.people.user+json"));
    }

    @Test
    public void shouldReturnResponseWithSecondContentType() throws Exception {
        interceptorChainProcessor.setupResponse("userId", "4444-5556", envelope().with(metadataWithDefaults()).build());

        final HttpResponse response = httpClient.execute(getRequestFor("/users/4444-5556", "application/vnd.people.user-summary+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        assertThat(response.getEntity().getContentType().getValue(), is("application/vnd.people.user-summary+json"));
    }

    @Test
    public void shouldDispatchUsersQueryWithQueryParams() throws Exception {
        interceptorChainProcessor.setupResponse("lastname", "Smith", envelope().with(metadataWithDefaults()).build());

        final String uri = new URIBuilder()
                .setPath("/users")
                .setParameter("lastname", "Smith")
                .setParameter("firstname", "John")
                .setParameter("height", "175.5")
                .setParameter("married", "True")
                .setParameter("age", "34")
                .build().toString();

        final HttpResponse response = httpClient.execute(getRequestFor(uri, "application/vnd.people.users+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
        final JsonEnvelope jsonEnvelope = interceptorChainProcessor.awaitForEnvelopeWithPayloadOf("lastname", "Smith");
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
        final String uri = new URIBuilder()
                .setPath("/users")
                .setParameter("firstname", "firstname")
                .build().toString();

        final HttpResponse response = httpClient.execute(getRequestFor(uri, "application/vnd.people.users+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn200WhenOptionalParameterIsNotProvided() throws Exception {
        interceptorChainProcessor.setupResponse("lastname", "lastname", envelope().with(metadataWithDefaults()).build());

        final String uri = new URIBuilder()
                .setPath("/users")
                .setParameter("lastname", "lastname")
                .build().toString();

        final HttpResponse response = httpClient.execute(getRequestFor(uri, "application/vnd.people.users+json"));

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));
    }

    @Test
    public void shouldAllowDependencyInjectionToOverrideCommonProviders() {
        assertThat(commonProviders.getClass() == DummyCommonProviders.class, is(true));
    }

    private HttpGet getRequestFor(final String uri, final String accept) throws UnsupportedEncodingException {
        final HttpGet request = new HttpGet(BASE_URI + uri);
        request.setHeader("Accept", accept);
        return request;
    }

    private HttpPost postRequestFor(final String uri, final String json, final String contentType, final String accept) throws UnsupportedEncodingException {
        final HttpPost request = postRequestFor(uri, json, contentType);
        request.setHeader("Accept", accept);
        return request;
    }

    private HttpPost postRequestFor(final String uri, final String json, final String contentType) throws UnsupportedEncodingException {
        final HttpPost request = new HttpPost(BASE_URI + uri);
        request.setEntity(new StringEntity(json));
        request.setHeader("Content-Type", contentType);
        return request;
    }

    private HttpPut putRequestFor(final String uri, final String json, final String contentType, final String accept) throws UnsupportedEncodingException {
        final HttpPut request = putRequestFor(uri, json, contentType);
        request.setHeader("Accept", accept);
        return request;
    }

    private HttpPut putRequestFor(final String uri, final String json, final String contentType) throws UnsupportedEncodingException {
        final HttpPut request = new HttpPut(BASE_URI + uri);
        request.setEntity(new StringEntity(json));
        request.setHeader("Content-Type", contentType);
        return request;
    }

    private HttpPatch patchRequestFor(final String uri, final String json, final String contentType, final String accept) throws UnsupportedEncodingException {
        final HttpPatch request = patchRequestFor(uri, json, contentType);
        request.setHeader("Accept", accept);
        return request;
    }

    private HttpPatch patchRequestFor(final String uri, final String json, final String contentType) throws UnsupportedEncodingException {
        final HttpPatch request = new HttpPatch(BASE_URI + uri);
        request.setEntity(new StringEntity(json));
        request.setHeader("Content-Type", contentType);
        return request;
    }

    private HttpDelete deleteRequestFor(final String uri, final String contentType) throws UnsupportedEncodingException {
        final HttpDelete request = new HttpDelete(BASE_URI + uri);
        request.setHeader("Content-Type", contentType);
        return request;
    }

    @ApplicationScoped
    public static class TestServiceContextNameProvider implements ServiceContextNameProvider {

        @Override
        public String getServiceContextName() {
            return "test-component";
        }
    }
}
