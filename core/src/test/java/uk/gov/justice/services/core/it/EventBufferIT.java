package uk.gov.justice.services.core.it;

import static co.unruly.matchers.OptionalMatchers.contains;
import static com.jayway.jsonassert.impl.matcher.IsEmptyCollection.empty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcherProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SynchronousDispatcherProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.extension.AnnotationScanner;
import uk.gov.justice.services.core.it.util.repository.StreamBufferOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.core.it.util.repository.StreamStatusOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.jms.JmsDestinations;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.core.util.TestEnvelopeRecorder;
import uk.gov.justice.services.event.buffer.core.repository.service.ConsecutiveEventBufferService;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.StreamBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streamstatus.StreamStatus;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@FrameworkComponent("CORE_TEST")
@Adapter(EVENT_LISTENER)
public class EventBufferIT {

    private static final String EVENT_ABC = "test.event-abc";
    private static final String LIQUIBASE_STREAM_STATUS_CHANGELOG_XML = "liquibase/event-buffer-changelog.xml";

    @Resource(name = "openejb/Resource/viewStore")
    private DataSource dataSource;

    @Inject
    private AsynchronousDispatcher asyncDispatcher;

    @Inject
    private AbcEventHandler abcEventHandler;

    @Inject
    private StreamBufferOpenEjbAwareJdbcRepository jdbcStreamBufferRepository;

    @Inject
    private StreamStatusOpenEjbAwareJdbcRepository statusRepository;

    @Module
    @Classes(cdi = true, value = {
            AbcEventHandler.class,
            AnnotationScanner.class,
            AsynchronousDispatcherProducer.class,
            SynchronousDispatcherProducer.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,

            SenderProducer.class,
            JmsSenderFactory.class,
            ComponentDestination.class,
            DefaultJmsEnvelopeSender.class,
            JmsDestinations.class,
            EnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapper.class,
            Enveloper.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            AccessControlService.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            JsonEnvelopeLoggerHelper.class,
            PolicyEvaluator.class,

            StreamBufferOpenEjbAwareJdbcRepository.class,
            StreamStatusOpenEjbAwareJdbcRepository.class,
            ConsecutiveEventBufferService.class,
            LoggerProducer.class

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
    public void shouldAddEventToBufferIfVersionNotOne() {

        UUID metadataId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();
        asyncDispatcher.dispatch(envelope);

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamBufferEvents, hasSize(1));
        assertThat(streamBufferEvents.get(0).getStreamId(), is(streamId));
        assertThat(streamBufferEvents.get(0).getVersion(), is(2L));

        assertThat(streamStatus.isPresent(), is(false));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, empty());

    }

    @Test
    public void shouldAddStatusVersionForNewStreamIdAndProcessIncomingEvent() {

        UUID metadataId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(1L))
                .build();
        asyncDispatcher.dispatch(envelope);

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamBufferEvents, empty());
        assertThat(streamStatus.isPresent(), is(true));
        assertThat(streamStatus.get().getVersion(), is(1L));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, hasSize(1));

        assertThat(handledEnvelopes.get(0).metadata().id(), is(metadataId));
        assertThat(handledEnvelopes.get(0).metadata().version(), contains(1l));
    }

    @Test
    public void shouldIncrementVersionWhenEventInOrder() throws SQLException, NamingException {

        UUID metadataId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        statusRepository.insert(new StreamStatus(streamId, 1L));


        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();


        asyncDispatcher.dispatch(envelope);

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamBufferEvents, empty());
        assertThat(streamStatus.isPresent(), is(true));
        assertThat(streamStatus.get().getVersion(), is(2L));

    }

    @Test
    public void shouldNotIncrementVersionWhenEventNotInOrder() throws SQLException, NamingException {

        UUID metadataId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();

        statusRepository.insert(new StreamStatus(streamId, 2L));

        asyncDispatcher.dispatch(envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(4L))
                .build());

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamBufferEvents, hasSize(1));
        assertThat(streamBufferEvents.get(0).getStreamId(), is(streamId));
        assertThat(streamBufferEvents.get(0).getVersion(), is(4L));
        assertThat(streamStatus.isPresent(), is(true));
        assertThat(streamStatus.get().getVersion(), is(2L));

    }

    @Test
    public void shouldReleaseBufferWhenMissingEventArrives() throws SQLException, NamingException {

        UUID metadataId2 = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId2, EVENT_ABC)
                        .withStreamId(streamId).withVersion(2L))
                .build();


        statusRepository.insert(new StreamStatus(streamId, 1l));

        UUID metadataId3 = UUID.randomUUID();
        UUID metadataId4 = UUID.randomUUID();
        UUID metadataId5 = UUID.randomUUID();

        jdbcStreamBufferRepository.insert(
                new StreamBufferEvent(streamId, 3l, envelope()
                        .with(metadataOf(metadataId3, EVENT_ABC)
                                .withStreamId(streamId).withVersion(3l)).toJsonString()
                )
        );

        jdbcStreamBufferRepository.insert(
                new StreamBufferEvent(streamId, 4L, envelope()
                        .with(metadataOf(metadataId4, EVENT_ABC)
                                .withStreamId(streamId).withVersion(4L)).toJsonString()
                )
        );

        jdbcStreamBufferRepository.insert(
                new StreamBufferEvent(streamId, 5L, envelope()
                        .with(metadataOf(metadataId5, EVENT_ABC)
                                .withStreamId(streamId).withVersion(5L)).toJsonString()
                )
        );

        asyncDispatcher.dispatch(envelope);

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamStatus.isPresent(), is(true));
        assertThat(streamStatus.get().getVersion(), is(5L));

        final List<JsonEnvelope> handledEnvelopes = abcEventHandler.recordedEnvelopes();
        assertThat(handledEnvelopes, hasSize(4));

        assertThat(handledEnvelopes.get(0).metadata().id(), is(metadataId2));
        assertThat(handledEnvelopes.get(0).metadata().version(), contains(2l));

        assertThat(handledEnvelopes.get(1).metadata().id(), is(metadataId3));
        assertThat(handledEnvelopes.get(1).metadata().version(), contains(3l));

        assertThat(handledEnvelopes.get(2).metadata().id(), is(metadataId4));
        assertThat(handledEnvelopes.get(2).metadata().version(), contains(4l));

        assertThat(handledEnvelopes.get(3).metadata().id(), is(metadataId5));
        assertThat(handledEnvelopes.get(3).metadata().version(), contains(5l));


        assertThat(streamBufferEvents, hasSize(0));
    }

    @Test
    public void shouldIgnoreEventWithSupersededVersion() throws SQLException, NamingException {

        UUID metadataId = UUID.randomUUID();
        UUID streamId = UUID.randomUUID();
        final JsonEnvelope envelope = envelope()
                .with(metadataOf(metadataId, EVENT_ABC)
                        .withStreamId(streamId).withVersion(1L))
                .build();

        final StreamBufferEvent streamBufferEvent2 = new StreamBufferEvent(streamId, 4L, "payload");
        final StreamBufferEvent streamBufferEvent3 = new StreamBufferEvent(streamId, 5L, "payload");


        statusRepository.insert(new StreamStatus(streamId, 2L));
        jdbcStreamBufferRepository.insert(streamBufferEvent2);
        jdbcStreamBufferRepository.insert(streamBufferEvent3);

        asyncDispatcher.dispatch(envelope);

        List<StreamBufferEvent> streamBufferEvents = jdbcStreamBufferRepository.streamById(streamId).collect(toList());
        Optional<StreamStatus> streamStatus = statusRepository.findByStreamId(streamId);

        assertThat(streamBufferEvents, hasSize(2));
        assertThat(streamStatus.isPresent(), is(true));
        assertThat(streamStatus.get().getVersion(), is(2L));
        assertThat(abcEventHandler.recordedEnvelopes(), empty());
    }

    private void initDatabase() throws Exception {
        Liquibase liquibase = new Liquibase(LIQUIBASE_STREAM_STATUS_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        liquibase.dropAll();
        liquibase.update("");
    }

    @ServiceComponent(EVENT_LISTENER)
    @ApplicationScoped
    public static class AbcEventHandler extends TestEnvelopeRecorder {

        @Handles(EVENT_ABC)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }

    }

}
