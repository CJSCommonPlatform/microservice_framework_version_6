package uk.gov.justice.services.adapter.direct;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.schema.catalog.CatalogProducer;
import uk.gov.justice.schema.catalog.util.ClasspathResourceLoader;
import uk.gov.justice.schema.catalog.util.UriResolver;
import uk.gov.justice.schema.catalog.util.UrlConverter;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainObserver;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.interceptor.InterceptorChainProvider;
import uk.gov.justice.services.core.json.DefaultFileSystemUrlResolverStrategy;
import uk.gov.justice.services.core.json.FileBasedJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.PayloadExtractor;
import uk.gov.justice.services.core.json.SchemaCatalogAwareJsonSchemaValidator;
import uk.gov.justice.services.core.mapping.DefaultSchemaIdMappingCache;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.core.mapping.SchemaIdMappingObserver;
import uk.gov.justice.services.core.requester.RequesterProducer;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.common.envelope.EnvelopeRecordingInterceptor;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class DirectAdapterIT {


    private static final String GET_USER_ACTION = "people.get-user";

    @Inject
    QueryViewDirectAdapter directAdapter;

    @Inject
    EnvelopeRecordingInterceptor testInterceptor;

    @Inject
    GetUserRecordingHandler testHandler;

    @Module
    @Classes(cdi = true, value = {
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            FileBasedJsonSchemaValidator.class,
            DefaultEnvelopeConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultTraceLogger.class,
            EnvelopeValidationExceptionHandlerProducer.class,


            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EnvelopeRecordingInterceptor.class,
            TestQueryApiInterceptorChainProvider.class,

            SenderProducer.class,
            DefaultJmsEnvelopeSender.class,
            EnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectMapper.class,

            DispatcherCache.class,
            DispatcherFactory.class,
            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,

            JndiBasedServiceContextNameProvider.class,
            ValueProducer.class,
            GlobalValueProducer.class,

            QueryViewDirectAdapter.class,
            GetUserRecordingHandler.class,

            DefaultFileSystemUrlResolverStrategy.class,
            JsonSchemaLoader.class,

            UriResolver.class,
            UrlConverter.class,
            SchemaCatalogAwareJsonSchemaValidator.class,
            PayloadExtractor.class,
            NameToMediaTypeConverter.class,
            DefaultSchemaIdMappingCache.class,
            SchemaIdMappingObserver.class,
            ClasspathResourceLoader.class,

            CatalogProducer.class,
            SchemaCatalogService.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("direct-adapter-test")
                .addServlet("TestApp", Application.class.getName());
    }


    @Test
    public void shouldProcessEnvelopePassedToAdapter() throws Exception {

        final JsonEnvelope envelopePassedToAdapter = envelope().with(metadataWithRandomUUID(GET_USER_ACTION)).build();
        directAdapter.process(envelopePassedToAdapter);

        assertThat(testInterceptor.firstRecordedEnvelope(), is(envelopePassedToAdapter));
        assertThat(testHandler.firstRecordedEnvelope(), is(envelopePassedToAdapter));

    }

    @Test
    public void shouldReturnEnvelopeReturnedByHandler() {
        final JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();
        testHandler.setUpResponse(responseEnvelope);

        assertThat(directAdapter.process(envelope().with(metadataWithRandomUUID(GET_USER_ACTION)).build()),
                is(responseEnvelope));

    }

    @ApplicationScoped
    public static class TestQueryApiInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return QUERY_VIEW;
        }

        @Override
        public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
            final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = new LinkedList<>();
            interceptorChainTypes.add(new ImmutablePair<>(1, EnvelopeRecordingInterceptor.class));
            return interceptorChainTypes;
        }
    }

    @ServiceComponent(QUERY_VIEW)
    @ApplicationScoped
    public static class GetUserRecordingHandler extends TestEnvelopeRecorder {

        private JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();

        public void setUpResponse(final JsonEnvelope responseEnvelope) {

            this.responseEnvelope = responseEnvelope;
        }

        @Handles(GET_USER_ACTION)
        public JsonEnvelope handle(final JsonEnvelope envelope) {
            record(envelope);
            return responseEnvelope;
        }

    }
}
