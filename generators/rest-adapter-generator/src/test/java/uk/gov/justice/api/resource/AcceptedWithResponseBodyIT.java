package uk.gov.justice.api.resource;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.api.CommandApiRestExampleApplication;
import uk.gov.justice.api.mapper.DefaultCommandApiUsersUserIdResourceActionMapper;
import uk.gov.justice.api.mapper.RestAdapterGeneratorMediaTypeToSchemaIdMapper;
import uk.gov.justice.schema.service.CatalogProducer;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.adapter.rest.application.CommonProviders;
import uk.gov.justice.services.adapter.rest.application.DefaultCommonProviders;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
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
import uk.gov.justice.services.adapter.rest.processor.response.AcceptedStatusEnvelopeEntityResponseStrategy;
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
import uk.gov.justice.services.messaging.logging.DefaultHttpTraceLoggerHelper;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
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
public class AcceptedWithResponseBodyIT {

    private static final String USER_MEDIA_TYPE = "application/vnd.people.user+json";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/command/api/rest/example";
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
            DefaultCommandApiUsersUserIdResource.class,
            DefaultCommandApiUsersUserIdResourceActionMapper.class,

            DefaultRestProcessor.class,
            AcceptedStatusEnvelopeEntityResponseStrategy.class,
            ResponseStrategyHelper.class,
            RestEnvelopeBuilderFactory.class,
            RecordingInterceptorChainProcessor.class,
            ObjectMapperProducer.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultCommonProviders.class,
            DummyCommonProviders.class,
            BadRequestExceptionMapper.class,
            JsonSchemaValidationInterceptor.class,
            LoggerRequestDataFilter.class,
            TestServiceContextNameProvider.class,
            FileBasedJsonSchemaValidator.class,
            JsonSchemaLoader.class,
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
                .addInitParam("TestApp", "javax.ws.rs.Application", CommandApiRestExampleApplication.class.getName());
    }

    @Test
    public void shouldReturn202WithResponseBodyWhenCreatingUser() throws Exception {
        interceptorChainProcessor.setupResponse("userUrn", "test",
                envelope().with(metadataWithDefaults()).withPayloadOf("user1234", "userName").build());

        final HttpResponse response = httpClient.execute(postRequestFor("/users/1234", JSON, USER_MEDIA_TYPE));

        assertThat(response.getStatusLine().getStatusCode(), is(ACCEPTED.getStatusCode()));

        final String responseBody = EntityUtils.toString(response.getEntity());
        with(responseBody)
                .assertThat("userName", equalTo("user1234"));
    }

    private HttpPost postRequestFor(final String uri, final String json, final String contentType) throws UnsupportedEncodingException {
        final HttpPost request = new HttpPost(BASE_URI + uri);
        request.setEntity(new StringEntity(json));
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
