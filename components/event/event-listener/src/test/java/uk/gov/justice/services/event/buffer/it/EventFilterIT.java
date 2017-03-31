package uk.gov.justice.services.event.buffer.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.DefaultAccessControlService;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DefaultDispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.envelope.EnvelopeValidationExceptionHandlerProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.eventfilter.AbstractEventFilter;
import uk.gov.justice.services.core.eventfilter.AllowAllEventFilter;
import uk.gov.justice.services.core.extension.AnnotationScanner;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainObserver;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.interceptor.InterceptorChainProvider;
import uk.gov.justice.services.core.jms.DefaultJmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.event.filter.EventFilterInterceptor;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
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
@FrameworkComponent("CORE_TEST")
@Adapter(EVENT_LISTENER)
public class EventFilterIT {

    private static final String EVENT_ABC = "test.event-abc";
    private static final String EVENT_DEF = "test.event-def";


    @Inject
    private InterceptorChainProcessor interceptorChainProcessor;

    @Inject
    private AbcEventHandler abcEventHandler;

    @Inject
    private DefEventHandler defEventHandler;


    @Module
    @Classes(cdi = true, value = {
            AbcEventHandler.class,
            DefEventHandler.class,
            AnnotationScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,

            SenderProducer.class,
            JmsSenderFactory.class,
            ComponentDestination.class,
            DefaultJmsEnvelopeSender.class,
            DefaultJmsDestinations.class,
            DefaultEnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapper.class,
            Enveloper.class,

            InterceptorChainProcessorProducer.class,
            InterceptorCache.class,
            InterceptorChainObserver.class,
            EventListenerInterceptorChainProvider.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            DefaultAccessControlService.class,
            DefaultDispatcherCache.class,
            DispatcherFactory.class,
            PolicyEvaluator.class,
            AllowAllEventFilter.class,
            AbcAllowingEventFilter.class,
            EventFilterInterceptor.class,

            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,

            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,
            DefaultJsonSchemaValidator.class,
            JsonSchemaLoader.class,
            DefaultTraceLogger.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("core-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Test
    public void shouldAllowEventABC() {

        final UUID metadataId = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(randomUUID())
                        .withVersion(1L))
                .build();

        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        assertThat(abcEventHandler.firstRecordedEnvelope(), not(nullValue()));
        assertThat(abcEventHandler.firstRecordedEnvelope().metadata().id(), equalTo(metadataId));
    }

    @Test
    public void shouldFilterOutEventDEF() {

        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataWithRandomUUID(EVENT_DEF)
                        .withStreamId(randomUUID())
                        .withVersion(1L))
                .build();

        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        assertThat(defEventHandler.recordedEnvelopes(), empty());
    }


    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_ABC)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class DefEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_DEF)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @ApplicationScoped
    @Alternative
    @Priority(2)
    public static class AbcAllowingEventFilter extends AbstractEventFilter {
        public AbcAllowingEventFilter() {
            super(EVENT_ABC);
        }
    }

    public static class EventListenerInterceptorChainProvider implements InterceptorChainProvider {

        @Override
        public String component() {
            return EVENT_LISTENER;
        }

        @Override
        public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
            final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = new ArrayList<>();
            interceptorChainTypes.add(new ImmutablePair<>(1, EventFilterInterceptor.class));
            return interceptorChainTypes;
        }
    }
}
