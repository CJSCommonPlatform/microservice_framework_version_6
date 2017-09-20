package uk.gov.justice.api;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapter;
import uk.gov.justice.services.adapter.direct.SynchronousDirectAdapterCache;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainObserver;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.json.DefaultFileSystemUrlResolverStrategy;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;

import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.requester.Requester;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@FrameworkComponent("QUERY_API")
public class QueryApiDirectClientIT {

    private static final String GET_USER_ACTION = "people.get-user";


    @Module
    @Classes(cdi = true, value = {
            ServiceComponentScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            DefaultJsonSchemaValidator.class,
            DefaultEnvelopeConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DefaultTraceLogger.class,
            EnvelopeValidationExceptionHandlerProducer.class,

            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EnvelopeRecordingInterceptor.class,

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

            DefaultFileSystemUrlResolverStrategy.class,
            JsonSchemaLoader.class,

            DirectQueryApi2QueryViewRestExampleClient.class,
            SynchronousDirectAdapterCache.class,
            TestDirectAdapter.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("direct-client-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Inject
    Requester requester;

    @Inject
    TestDirectAdapter testDirectAdapter;

    @Test
    public void shouldPassEnvelopeToAdapter() throws Exception {

        final JsonEnvelope envelopePassedToAdapter = envelope().with(metadataWithRandomUUID(GET_USER_ACTION)).build();
        requester.request(envelopePassedToAdapter);

        assertThat(testDirectAdapter.firstRecordedEnvelope(), is(envelopePassedToAdapter));

    }


    @Test
    public void shouldReturnEnvelopeReturnedByAdapter() {
        final JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();
        testDirectAdapter.setUpResponse(responseEnvelope);

        assertThat(requester.request(envelope().with(metadataWithRandomUUID(GET_USER_ACTION)).build()),
                is(responseEnvelope));

    }

    @DirectAdapter("QUERY_VIEW")
    @ApplicationScoped
    public static class TestDirectAdapter extends TestEnvelopeRecorder implements SynchronousDirectAdapter {

        private JsonEnvelope responseEnvelope = envelope().with(metadataWithDefaults()).build();

        public void setUpResponse(final JsonEnvelope responseEnvelope) {
            this.responseEnvelope = responseEnvelope;
        }

        @Override
        public JsonEnvelope process(JsonEnvelope envelope) {
            record(envelope);
            return responseEnvelope;
        }
    }
}
