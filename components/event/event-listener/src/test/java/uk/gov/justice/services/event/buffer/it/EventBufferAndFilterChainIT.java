package uk.gov.justice.services.event.buffer.it;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
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
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.interceptor.InterceptorObserver;
import uk.gov.justice.services.core.jms.DefaultJmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.json.DefaultJsonSchemaValidator;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.event.buffer.EventBufferInterceptor;
import uk.gov.justice.services.event.buffer.core.service.ConsecutiveEventBufferService;
import uk.gov.justice.services.event.buffer.it.util.buffer.AnsiSQLBufferInitialisationStrategyProducer;
import uk.gov.justice.services.event.buffer.it.util.repository.StreamBufferOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.event.buffer.it.util.repository.StreamStatusOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.event.filter.EventFilterInterceptor;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.UUID;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@Adapter(EVENT_LISTENER)
public class EventBufferAndFilterChainIT {

    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    private static final String EVENT_SUPPORTED_ABC = "test.event-abc";
    private static final String EVENT_UNSUPPORTED_DEF = "test.event-def";

    @Resource(name = "openejb/Resource/viewStore")
    private DataSource dataSource;

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
            EnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapperProducer.class,
            Enveloper.class,

            InterceptorChainProcessorProducer.class,
            InterceptorChainProcessor.class,
            InterceptorCache.class,
            InterceptorObserver.class,

            AllowAllEventFilter.class,
            SupportedEventAllowingEventFilter.class,
            EventFilterInterceptor.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            AccessControlService.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            PolicyEvaluator.class,

            StreamBufferOpenEjbAwareJdbcRepository.class,
            StreamStatusOpenEjbAwareJdbcRepository.class,
            ConsecutiveEventBufferService.class,
            AnsiSQLBufferInitialisationStrategyProducer.class,
            EventBufferInterceptor.class,
            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class,
            UtcClock.class,

            GlobalValueProducer.class,
            EnvelopeValidationExceptionHandlerProducer.class,

            DefaultJsonSchemaValidator.class,
            JsonSchemaLoader.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("core-test")
                .addServlet("TestApp", Application.class.getName());
    }

    @Before
    public void init() throws Exception {
        initDatabase();
    }

    @Test
    public void shouldAllowUnsupportedEventThroughBufferAndFilterOutAfterwards() {

        final UUID metadataId1 = randomUUID();
        final UUID metadataId2 = randomUUID();
        final UUID metadataId3 = randomUUID();
        final UUID streamId = randomUUID();

        interceptorChainProcessor.process(
                envelope()
                        .with(metadataOf(metadataId2, EVENT_UNSUPPORTED_DEF)
                                .withStreamId(streamId)
                                .withVersion(2L))
                        .build());
        interceptorChainProcessor.process(
                envelope()
                        .with(metadataOf(metadataId3, EVENT_SUPPORTED_ABC)
                                .withStreamId(streamId)
                                .withVersion(3L))
                        .build());


        interceptorChainProcessor.process(
                envelope()
                        .with(metadataOf(metadataId1, EVENT_SUPPORTED_ABC)
                                .withStreamId(streamId)
                                .withVersion(1L))
                        .build());

        assertThat(abcEventHandler.recordedEnvelopes(), not(empty()));
        assertThat(abcEventHandler.recordedEnvelopes().get(0).metadata().id(), equalTo(metadataId1));
        assertThat(abcEventHandler.recordedEnvelopes().get(0).metadata().version(), contains(1L));
        assertThat(abcEventHandler.recordedEnvelopes().get(1).metadata().id(), equalTo(metadataId3));
        assertThat(abcEventHandler.recordedEnvelopes().get(1).metadata().version(), contains(3L));


        assertThat(defEventHandler.recordedEnvelopes(), empty());
    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_SUPPORTED_ABC)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class DefEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_UNSUPPORTED_DEF)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

    @ApplicationScoped
    @Alternative
    @Priority(2)
    public static class SupportedEventAllowingEventFilter extends AbstractEventFilter {
        public SupportedEventAllowingEventFilter() {
            super(EVENT_SUPPORTED_ABC);
        }
    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }
}
