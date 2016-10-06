package uk.gov.justice.services.core.aggregate;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.PrivateAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.DateTimeProvider;
import uk.gov.justice.services.core.aggregate.event.EventA;
import uk.gov.justice.services.core.aggregate.event.EventB;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.source.core.EnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.repository.EventLogOpenEjbAwareJdbcRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class DefaultAggregateServiceIT {

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String TEST_AGGREGATE_CLASS_NAME = "GeneratedTestAggregate";

    private static final String TEST_AGGREGATE_PACKAGE = "uk.gov.justice.services.core.aggregate";

    private static final String TYPE = TEST_AGGREGATE_PACKAGE + ".DefaultAggregateServiceIT$TestAggregate";

    private static final int SNAPSHOT_THRESHOLD = 25;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private EventSource eventSource;

    @Inject
    private DefaultAggregateService aggregateService;

    @Inject
    private EventLogOpenEjbAwareJdbcRepository eventLogRepository;

    @Module
    @Classes(cdi = true, value = {
            LoggerProducer.class,

            AbstractJdbcRepository.class,
            JdbcEventRepository.class,
            EventRepository.class,
            EventLogOpenEjbAwareJdbcRepository.class,

            DefaultAggregateService.class,

            EventSource.class,
            EnvelopeEventStream.class,
            EventStreamManager.class,

            EnvelopeConverter.class,
            EventLogConverter.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectMapperProducer.class,

            DummyJmsEventPublisher.class,
            DummyJmsEnvelopeSender.class,
            DateTimeProvider.class

    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("aggregateservice-test")
                .addServlet("AggregateServiceApp", Application.class.getName());
    }

    @Before
    public void init() throws Exception {
        initDatabase();
    }

    private void initDatabase() throws Exception {

        Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        snapshotLiquidBase.dropAll();
        snapshotLiquidBase.update("");

    }

    private Stream<JsonEnvelope> envelopes(final int numberOfEnvelopes, String eventName) {
        List<JsonEnvelope> envelopes = new LinkedList<>();
        for (int i = 1; i <= numberOfEnvelopes; i++) {
            envelopes.add(envelope().with(metadataWithRandomUUID(eventName).withStreamId(STREAM_ID)).withPayloadOf("value", "name").build());
        }
        return envelopes.stream();
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() {
        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
    }

    @Test
    public void shouldCreateAggregateFromSingletonStream() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);
        aggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));

        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        eventStream.append(envelopes(1, "context.eventA"));

        aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(1));
        assertThat(aggregate.recordedEvents().get(0).getClass(), equalTo(EventA.class));
        MatcherAssert.assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(1));

    }

    @Test
    public void shouldCreateAggregateFromStreamOfTwo() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        aggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));
        aggregateService.register(new EventFoundEvent(EventB.class, "context.eventB"));


        TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        eventStream.append(envelopes(1, "context.eventA"));
        eventStream.append(envelopes(1, "context.eventB"));

        aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(2));
        assertThat(aggregate.recordedEvents().get(0).getClass(), equalTo(EventA.class));
        assertThat(aggregate.recordedEvents().get(1).getClass(), equalTo(EventB.class));
        MatcherAssert.assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnregisteredEvent() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        eventStream.append(envelopes(1, "context.eventA"));

        aggregateService.get(eventStream, TestAggregate.class);

    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));

        aggregateService.get(eventStream, PrivateAggregate.class);
    }


    public static class DummyJmsEventPublisher implements EventPublisher {

        @Override
        public void publish(JsonEnvelope envelope) {

        }
    }

    @ApplicationScoped
    public static class DummyJmsEnvelopeSender implements JmsEnvelopeSender {

        @Override
        public void send(JsonEnvelope envelope, Destination destination) {

        }

        @Override
        public void send(JsonEnvelope envelope, String destinationName) {

        }
    }

}