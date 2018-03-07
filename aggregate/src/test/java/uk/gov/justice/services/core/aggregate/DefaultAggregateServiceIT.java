package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.justice.services.core.h2.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.PrivateAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.event.EventA;
import uk.gov.justice.services.core.aggregate.event.EventB;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.core.json.DefaultFileSystemUrlResolverStrategy;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.DefaultEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.core.DefaultEventSource;
import uk.gov.justice.services.eventsourcing.source.core.EnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.PublishingEventAppender;
import uk.gov.justice.services.eventsourcing.source.core.SystemEventService;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.jdbc.persistence.AbstractJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class DefaultAggregateServiceIT {

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String SQL_EVENT_LOG_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_log WHERE stream_id=? ";
    private static final String SQL_EVENT_STREAM_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_stream WHERE stream_id=? ";

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private EventSource eventSource;

    @Inject
    private DefaultAggregateService aggregateService;

    @Inject
    private Clock clock;

    @Module
    @Classes(cdi = true, value = {
            LoggerProducer.class,

            AbstractJdbcRepository.class,
            DefaultEventRepository.class,
            TestEventInsertionStrategyProducer.class,

            DefaultAggregateService.class,
            EventStreamJdbcRepository.class,
            EventJdbcRepository.class,
            JdbcRepositoryHelper.class,
            JdbcDataSourceProvider.class,
            EventStreamManager.class,
            EventStreamJdbcRepository.class,
            DefaultEventSource.class,
            EnvelopeEventStream.class,
            EventStreamManager.class,
            DefaultEnveloper.class,
            ObjectToJsonValueConverter.class,
            ObjectToJsonObjectConverter.class,
            EventAppender.class,
            PublishingEventAppender.class,
            SystemEventService.class,
            DefaultEnvelopeConverter.class,
            EventConverter.class,
            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectMapperProducer.class,

            DummyJmsEventPublisher.class,
            DummyJmsEnvelopeSender.class,
            UtcClock.class,

            GlobalValueProducer.class,

            DefaultFileSystemUrlResolverStrategy.class

    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("aggregateservice-test")
                .addServlet("AggregateServiceApp", Application.class.getName());
    }

    @Configuration
    public Properties configuration() {
        return createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addH2EventStore()
                .build();
    }

    @Before
    public void init() throws Exception {
        final InitialContext initialContext = new InitialContext();
        initialContext.bind("java:/app/DefaultAggregateServiceIT/DS.eventstore", dataSource);
        initDatabase();
    }

    @Test
    public void shouldCreateAggregateFromEmptyStream() {
        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        final TestAggregate aggregate = aggregateService.get(eventStream, TestAggregate.class);

        assertThat(aggregate, notNullValue());
        assertThat(aggregate.recordedEvents(), empty());
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, STREAM_ID), is(0));
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
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, STREAM_ID), is(1));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, STREAM_ID), is(1));
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
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, STREAM_ID), is(2));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, STREAM_ID), is(1));

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

        final Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
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
        public void publish(final JsonEnvelope envelope) {

        }

    }

    @ApplicationScoped
    public static class DummyJmsEnvelopeSender implements JmsEnvelopeSender {

        @Override
        public void send(final JsonEnvelope envelope, final Destination destination) {

        }

        @Override
        public void send(final JsonEnvelope envelope, final String destinationName) {

        }
    }


    private int rowCount(final String sql, final Object arg) {

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, arg);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Exception getting count of entries from [%s] for  [%s]", sql, arg), e);
        }

    }

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new AnsiSQLEventLogInsertionStrategy();
        }
    }
}
