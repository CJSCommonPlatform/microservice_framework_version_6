package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.justice.services.core.h2.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.aggregate.classloader.CustomClassLoaderObjectInputStreamStrategy;
import uk.gov.justice.domain.aggregate.classloader.DynamicAggregateTestClassGenerator;
import uk.gov.justice.domain.aggregate.classloader.DynamicallyLoadingClassLoader;
import uk.gov.justice.domain.event.EventA;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotRepository;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcBasedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.PublishingEventAppender;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEventSource;
import uk.gov.justice.services.eventsourcing.source.core.SystemEventService;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotStrategy;
import uk.gov.justice.services.jdbc.persistence.DataSourceJndiNameProvider;
import uk.gov.justice.services.jdbc.persistence.InitialContextProvider;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class SnapshotAwareAggregateServiceIT {

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String LIQUIBASE_SNAPSHOT_STORE_CHANGELOG_XML = "liquibase/snapshot-store-db-changelog.xml";

    private static final String AGGREGATE_INTERFACE_FULL_NAME = "uk.gov.justice.domain.aggregate.Aggregate";

    private static final String TEST_AGGREGATE_CLASS_NAME = "GeneratedTestAggregate";

    private static final String TEST_AGGREGATE_PACKAGE = "uk.gov.justice.domain.aggregate";

    private static final String TEST_AGGREGATE_FULL_NAME = format("%s.%s", TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

    private static final String TEST_AGGREGATE_COMPILED_CLASS = format("%s/%s.class", TEST_AGGREGATE_PACKAGE.replace(".", "/"), TEST_AGGREGATE_CLASS_NAME);

    private static final String TYPE = TEST_AGGREGATE_PACKAGE + ".TestAggregate";

    private static final String SQL_EVENT_LOG_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_log WHERE stream_id=? ";
    private static final String SQL_EVENT_STREAM_COUNT_BY_STREAM_ID = "SELECT count(*) FROM event_stream WHERE stream_id=? ";

    private static final long SNAPSHOT_THRESHOLD = 25L;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private SnapshotRepository snapshotRepository;

    @Inject
    private SnapshotAwareEventSource eventSource;

    @Inject
    private SnapshotAwareAggregateService aggregateService;

    @Inject
    private DefaultAggregateService defaultAggregateService;

    @Inject
    private Clock clock;

    @Inject
    private DefaultSnapshotService snapshotService;


    @Module
    @org.apache.openejb.testing.Classes(cdi = true, value = {
            ObjectInputStreamStrategy.class,
            CustomClassLoaderObjectInputStreamStrategy.class,
            DefaultObjectInputStreamStrategy.class,
            SnapshotJdbcRepository.class,
            JdbcDataSourceProvider.class,

            EventStreamJdbcRepository.class,
            JdbcBasedEventRepository.class,
            TestEventInsertionStrategyProducer.class,
            EventJdbcRepository.class,
            JdbcRepositoryHelper.class,
            JdbcDataSourceProvider.class,

            LoggerProducer.class,

            EventConverter.class,
            DefaultEnvelopeConverter.class,
            StringToJsonObjectConverter.class,
            DefaultJsonObjectEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectMapperProducer.class,

            JmsEventPublisher.class,
            DummyJmsEnvelopeSender.class,
            DefaultEventDestinationResolver.class,
            DefaultAggregateService.class,
            SnapshotAwareAggregateService.class,
            SnapshotAwareEventSource.class,
            SnapshotAwareEnvelopeEventStream.class,
            EventStreamManager.class,
            DefaultEnveloper.class,
            ObjectToJsonValueConverter.class,
            SystemEventService.class,
            EventAppender.class,
            PublishingEventAppender.class,
            DefaultSnapshotStrategy.class,
            ValueProducer.class,
            DefaultSnapshotService.class,
            UtcClock.class,
            TestServiceContextNameProvider.class,
            GlobalValueProducer.class,
            ObjectToJsonObjectConverter.class,

            DataSourceJndiNameProvider.class,
            InitialContextProvider.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("snapshot-test")
                .addServlet("SnapShotApp", Application.class.getName());
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
        initialContext.bind("java:/app/SnapshotAwareAggregateServiceIT/DS.eventstore", dataSource);
        initEventDatabase();
        defaultAggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));
    }

    @Test
    public void shouldStoreABrandNewSnapshotWhenEventCountInTheStreamReachesThreshold() throws Exception {

        final UUID streamId = randomUUID();
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(streamId, TestAggregate.class);

        final TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(streamId));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));

        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(25));
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
        assertThat(aggregateFromSnapshot.recordedEvents().size(), is(25));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldNotStoreABrandNewSnapshotWhenEventCountInTheStreamReachesThresholdNotMet() throws Exception {

        final UUID streamId = randomUUID();
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD - 2);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(streamId, TestAggregate.class);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(false));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(23));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldNotCreateNewSnapshotOnAggregateChangeWhenWeJustOneExistingSnapshots() throws Exception {

        final Class aggregateClass = TestAggregate.class;

        final UUID streamId = randomUUID();
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(streamId, aggregateClass);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));

        final EventStream updatedStream = eventSource.getStreamById(streamId);
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD - 2);

        final Optional<AggregateSnapshot<TestAggregate>> snapshotChanged = snapshotRepository.getLatestSnapshot(streamId, aggregateClass);
        assertThat(snapshotChanged, not(nullValue()));
        assertThat(snapshotChanged.isPresent(), equalTo(true));
        assertThat(snapshotChanged.get().getType(), equalTo(aggregateClass.getName()));
        assertThat(snapshotChanged.get().getStreamId(), equalTo(streamId));
        assertThat(snapshotChanged.get().getVersionId(), equalTo(25L));

        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(48));
        final TestAggregate aggregateFromSnapshot = snapshotChanged.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldCreateNewSnapshotOnAggregateChangeWhenWeHaveMultipleExistingSnapshots() throws Exception {

        final Class aggregateClass = TestAggregate.class;
        final UUID streamId = randomUUID();

        final long initialNumberOfSnapshots = 4;
        for (int i = 0; i < initialNumberOfSnapshots; i++) {
            appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);
        }

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(streamId, aggregateClass);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));


        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD - 2);


        final Optional<AggregateSnapshot> newSnapshot = snapshotRepository.getLatestSnapshot(streamId, aggregateClass);
        assertThat(newSnapshot, not(nullValue()));
        assertThat(newSnapshot.isPresent(), equalTo(true));
        assertThat(newSnapshot.get().getType(), equalTo(aggregateClass.getName()));
        assertThat(newSnapshot.get().getStreamId(), equalTo(streamId));
        assertThat(newSnapshot.get().getVersionId(), equalTo(initialNumberOfSnapshots * SNAPSHOT_THRESHOLD));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(123));
        TestAggregate aggregateFromSnapshot2 = (TestAggregate) newSnapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot2.numberOfAppliedEvents(), is(100));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldNotStoreABrandNewSnapshotWhenStrategyDoesNotMandateSavingSnapshot() throws Exception {

        final UUID streamId = randomUUID();
        final EventStream stream = eventSource.getStreamById(streamId);

        final TestAggregate aggregate = aggregateService.get(stream, TestAggregate.class);
        stream.append(createEventAndApply(streamId, 24, aggregate));


        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(streamId, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(false));

        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(24));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldNotStoreANewSnapshotOnTopOfExistingSnapshotsWhenThresholdNotMet() throws Exception {

        final UUID streamId = randomUUID();
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);

        final EventStream stream = eventSource.getStreamById(streamId);
        final TestAggregate aggregate = aggregateService.get(stream, TestAggregate.class);
        stream.append(createEventAndApply(streamId, SNAPSHOT_THRESHOLD - 2, aggregate));

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(streamId, TestAggregate.class);
        assertThat(snapshot, notNullValue());
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(streamId));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(48));

        TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldStoreANewSnapshotOnTopOfExistingSnapshot() throws Exception {

        final UUID streamId = randomUUID();
        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);

        appendEventsViaAggregate(streamId, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(streamId, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(streamId));
        assertThat(snapshot.get().getVersionId(), equalTo(50L));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(50));

        TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(50));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    @Test
    public void shouldRebuildSnapshotOnAggregateModelChange() throws Exception {

        final UUID streamId = randomUUID();
        final DynamicAggregateTestClassGenerator classGenerator = new DynamicAggregateTestClassGenerator();

        final Class oldAggregateClass = classGenerator.generatedTestAggregateClassOf(1L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        final long initialNumberOfSnapshots = 4;

        for (int i = 1; i <= initialNumberOfSnapshots; i++) {
            createEventStreamAndApply(streamId, SNAPSHOT_THRESHOLD, "context.eventA", oldAggregateClass);
        }

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(streamId, oldAggregateClass);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(100));


        final Class newAggregateClass = classGenerator.generatedTestAggregateClassOf(2L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        snapshotService.setStreamStrategy(
                new CustomClassLoaderObjectInputStreamStrategy(classLoaderWithGeneratedAggregateLoaded()));

        createEventStreamAndApply(streamId, SNAPSHOT_THRESHOLD - 2, "context.eventA", newAggregateClass);

        final Optional<AggregateSnapshot> newSnapshot = snapshotRepository.getLatestSnapshot(streamId, newAggregateClass);
        assertThat(newSnapshot, not(nullValue()));
        assertThat(newSnapshot.isPresent(), equalTo(true));
        assertThat(newSnapshot.get().getType(), equalTo(newAggregateClass.getName()));
        assertThat(newSnapshot.get().getStreamId(), equalTo(streamId));
        assertThat(newSnapshot.get().getVersionId(), equalTo(123L));
        assertThat(rowCount(SQL_EVENT_LOG_COUNT_BY_STREAM_ID, streamId), is(123));
        assertThat(rowCount(SQL_EVENT_STREAM_COUNT_BY_STREAM_ID, streamId), is(1));
    }

    private void initEventDatabase() throws Exception {

        final Liquibase eventStoreLiquibase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        eventStoreLiquibase.dropAll();
        eventStoreLiquibase.update("");
        final Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_SNAPSHOT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        snapshotLiquidBase.update("");

    }

    private DynamicallyLoadingClassLoader classLoaderWithGeneratedAggregateLoaded() throws ClassNotFoundException {

        final DynamicallyLoadingClassLoader classLoader = new DynamicallyLoadingClassLoader(this.getClass(), TEST_AGGREGATE_CLASS_NAME, TEST_AGGREGATE_COMPILED_CLASS);
        classLoader.loadClass(AGGREGATE_INTERFACE_FULL_NAME);
        classLoader.loadClass(TEST_AGGREGATE_FULL_NAME);

        return classLoader;
    }

    private void appendEventsViaAggregate(final UUID streamId, final long eventCount) throws Exception {

        final EventStream eventStream = eventSource.getStreamById(streamId);
        TestAggregate aggregateRebuilt = aggregateService.get(eventStream, TestAggregate.class);
        eventStream.append(createEventAndApply(streamId, eventCount, aggregateRebuilt));
    }

    private Stream<JsonEnvelope> createEventAndApply(final UUID streamId, final long count, final TestAggregate aggregate) {
        final List<JsonEnvelope> envelopes = new LinkedList<>();
        for (int i = 1; i <= count; i++) {
            final JsonEnvelope envelope = envelope()
                    .with(metadataWithRandomUUID("context.eventA")
                            .createdAt(clock.now())
                            .withStreamId(streamId))
                    .withPayloadOf("value", "name")
                    .build();
            aggregate.addEvent(envelope);
            envelopes.add(envelope);
        }
        return envelopes.stream();
    }

    private <T extends Aggregate> void createEventStreamAndApply(final UUID streamId, final long count, final String eventName, final Class<T> aggregateClass) throws EventStreamException {

        final EventStream eventStream = eventSource.getStreamById(streamId);
        final T aggregate = aggregateService.get(eventStream, aggregateClass);

        final List<JsonEnvelope> envelopes = new LinkedList<>();

        for (int i = 1; i <= count; i++) {

            final JsonEnvelope envelope = envelope()
                    .with(metadataWithRandomUUID(eventName)
                            .createdAt(clock.now())
                            .withStreamId(streamId))
                    .withPayloadOf("value", "name")
                    .build();

            aggregate.apply(new EventA(String.valueOf(i)));
            envelopes.add(envelope);
        }
        eventStream.append(envelopes.stream());
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
    public static class DummyJmsEnvelopeSender implements JmsEnvelopeSender {

        @Override
        public void send(final JsonEnvelope envelope, final Destination destination) {

        }

        @Override
        public void send(final JsonEnvelope envelope, final String destinationName) {

        }
    }

    @ApplicationScoped
    public static class TestServiceContextNameProvider implements ServiceContextNameProvider {

        @Override
        public String getServiceContextName() {
            return "test-component";
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
