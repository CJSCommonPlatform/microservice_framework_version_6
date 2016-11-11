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
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.event.EventA;
import uk.gov.justice.services.core.aggregate.event.EventB;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.source.core.DefaultEventSource;
import uk.gov.justice.services.eventsourcing.source.core.EnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.repository.EventLogOpenEjbAwareJdbcRepository;

import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class DefaultAggregateServiceIT {

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String TEST_AGGREGATE_PACKAGE = "uk.gov.justice.services.core.aggregate";

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private EventSource eventSource;

    @Inject
    private DefaultAggregateService aggregateService;

    @Inject
    private EventLogOpenEjbAwareJdbcRepository eventLogRepository;

    @Inject
    private Clock clock;

    @Module
    @Classes(cdi = true, value = {
            LoggerProducer.class,

            AbstractJdbcRepository.class,
            JdbcEventRepository.class,
            EventRepository.class,
            EventLogOpenEjbAwareJdbcRepository.class,
            TestEventLogInsertionStrategyProducer.class,

            DefaultAggregateService.class,

            DefaultEventSource.class,
            EnvelopeEventStream.class,
            EventStreamManager.class,
            EventAppender.class,

            EnvelopeConverter.class,
            EventLogConverter.class,
            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectMapperProducer.class,

            DummyJmsEventPublisher.class,
            DummyJmsEnvelopeSender.class,
            UtcClock.class,

            GlobalValueProducer.class,

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

    @Test
    public void shouldCreateAggregateFromEmptyStream() {
        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        final TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
    }

    @Test
    public void shouldCreateAggregateFromSingletonStream() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);
        aggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));

        aggregateService.get(eventStream, TestAggregate.class);

        eventStream.append(Stream.of(envelopeFrom("context.eventA")));

        final TestAggregate aggregate = aggregateService.get(eventSource.getStreamById(STREAM_ID), TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(1));
        assertThat(aggregate.recordedEvents().get(0).getClass(), equalTo(EventA.class));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(1));

    }

    @Test
    public void shouldCreateAggregateFromStreamOfTwo() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        aggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));
        aggregateService.register(new EventFoundEvent(EventB.class, "context.eventB"));

        aggregateService.get(eventStream, TestAggregate.class);

        eventStream.append(Stream.of(envelopeFrom("context.eventA"), envelopeFrom("context.eventB")));

        final TestAggregate aggregate = aggregateService.get(eventSource.getStreamById(STREAM_ID), TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), hasSize(2));
        assertThat(aggregate.recordedEvents().get(0).getClass(), equalTo(EventA.class));
        assertThat(aggregate.recordedEvents().get(1).getClass(), equalTo(EventB.class));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnregisteredEvent() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        eventStream.append(Stream.of(envelopeFrom("context.eventA")));

        aggregateService.get(eventStream, TestAggregate.class);

    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionForNonInstantiatableEvent() throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        aggregateService.register(new EventFoundEvent(EventA.class, "eventA"));

        aggregateService.get(eventStream, PrivateAggregate.class);
    }

    private void initDatabase() throws Exception {

        Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        snapshotLiquidBase.dropAll();
        snapshotLiquidBase.update("");

    }

    private JsonEnvelope envelopeFrom(final String eventName) {
        return envelope()
                .with(metadataWithRandomUUID(eventName)
                        .createdAt(clock.now())
                        .withStreamId(STREAM_ID))
                .withPayloadOf("value", "name")
                .build();
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

    @ApplicationScoped
    public static class TestEventLogInsertionStrategyProducer {

        @Produces
        public EventLogInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }
}
