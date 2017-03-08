package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.aggregate.classloader.CustomClassLoaderObjectInputStreamStrategy;
import uk.gov.justice.domain.aggregate.classloader.DynamicAggregateTestClassGenerator;
import uk.gov.justice.domain.aggregate.classloader.DynamicallyLoadingClassLoader;
import uk.gov.justice.domain.event.EventA;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;
import uk.gov.justice.repository.EventLogOpenEjbAwareJdbcRepository;
import uk.gov.justice.repository.SnapshotOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.configuration.ValueProducer;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEventSource;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotStrategy;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import org.apache.openejb.testing.Module;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
public class SnapshotAwareAggregateServiceIT {

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String LIQUIBASE_SNAPSHOT_STORE_CHANGELOG_XML = "liquibase/snapshot-store-db-changelog.xml";

    private static final String AGGREGATE_INTERFACE_FULL_NAME = "uk.gov.justice.domain.aggregate.Aggregate";

    private static final String TEST_AGGREGATE_CLASS_NAME = "GeneratedTestAggregate";

    private static final String TEST_AGGREGATE_PACKAGE = "uk.gov.justice.domain.aggregate";

    private static final String TEST_AGGREGATE_FULL_NAME = format("%s.%s", TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

    private static final String TEST_AGGREGATE_COMPILED_CLASS = format("%s/%s.class", TEST_AGGREGATE_PACKAGE.replace(".", "/"), TEST_AGGREGATE_CLASS_NAME);

    private static final String TYPE = TEST_AGGREGATE_PACKAGE + ".TestAggregate";

    private static final long SNAPSHOT_THRESHOLD = 25L;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private uk.gov.justice.repository.SnapshotOpenEjbAwareJdbcRepository snapshotRepository;

    @Inject
    private EventLogOpenEjbAwareJdbcRepository eventLogRepository;

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

            SnapshotOpenEjbAwareJdbcRepository.class,
            EventLogOpenEjbAwareJdbcRepository.class,
            JdbcEventRepository.class,
            EventRepository.class,
            TestEventLogInsertionStrategyProducer.class,

            LoggerProducer.class,

            EventLogConverter.class,
            EnvelopeConverter.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
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
            EventAppender.class,
            DefaultSnapshotStrategy.class,
            ValueProducer.class,
            DefaultSnapshotService.class,
            UtcClock.class,
            TestServiceContextNameProvider.class,
            GlobalValueProducer.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("snapshot-test")
                .addServlet("SnapShotApp", Application.class.getName());
    }

    @Before
    public void init() throws Exception {
        initEventDatabase();
        defaultAggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));
    }

    @Test
    public void shouldStoreABrandNewSnapshotWhenEventCountInTheStreamReachesThreshold() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);

        final TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());

        assertThat(snapshot, IsNot.not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(25));
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
        assertThat(aggregateFromSnapshot.recordedEvents().size(), is(25));
    }

    @Test
    public void shouldNotStoreABrandNewSnapshotWhenEventCountInTheStreamReachesThresholdNotMet() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD - 2);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);

        assertThat(snapshot, IsNot.not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(false));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(23));
    }

    @Test
    public void shouldNotCreateNewSnapshotOnAggregateChangeWhenWeJustOneExistingSnapshots() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);
        final Class aggregateClass = TestAggregate.class;

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, aggregateClass);

        assertThat(snapshot, IsNot.not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD - 2);

        final Optional<AggregateSnapshot<TestAggregate>> snapshotChanged = snapshotRepository.getLatestSnapshot(STREAM_ID, aggregateClass);
        assertThat(snapshotChanged, IsNot.not(nullValue()));
        assertThat(snapshotChanged.isPresent(), equalTo(true));
        assertThat(snapshotChanged.get().getType(), equalTo(aggregateClass.getName()));
        assertThat(snapshotChanged.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshotChanged.get().getVersionId(), equalTo(25L));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(48));
        TestAggregate aggregateFromSnapshot = snapshotChanged.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
    }

    @Test
    public void shouldCreateNewSnapshotOnAggregateChangeWhenWeHaveMultipleExistingSnapshots() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        final Class aggregateClass = TestAggregate.class;

        final long initialNumberOfSnapshots = 4;
        for (int i = 0; i < initialNumberOfSnapshots; i++) {
            rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD);
        }

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, aggregateClass);

        assertThat(snapshot, IsNot.not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(initialNumberOfSnapshots));

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD - 2);


        final Optional<AggregateSnapshot> newSnapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, aggregateClass);
        assertThat(newSnapshot, IsNot.not(nullValue()));
        assertThat(newSnapshot.isPresent(), equalTo(true));
        assertThat(newSnapshot.get().getType(), equalTo(aggregateClass.getName()));
        assertThat(newSnapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(newSnapshot.get().getVersionId(), equalTo(initialNumberOfSnapshots * SNAPSHOT_THRESHOLD));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(4L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(123));
        TestAggregate aggregateFromSnapshot2 = (TestAggregate) newSnapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot2.numberOfAppliedEvents(), is(100));
    }

    @Test
    public void shouldNotStoreABrandNewSnapshotWhenStrategyDoesNotMandateSavingSnapshot() throws Exception {

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        TestAggregate aggregate = aggregateService.get(stream, TestAggregate.class);
        stream.append(createEventAndApply(24, "context.eventA", aggregate));


        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, IsNot.not(Matchers.nullValue()));
        assertThat(snapshot.isPresent(), equalTo(false));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(24));
    }

    @Test
    public void shouldNotStoreANewSnapshotOnTopOfExistingSnapshotsWhenThresholdNotMet() throws Exception {
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        rebuildAggregateAndApplyEvents(stream, SNAPSHOT_THRESHOLD);

        TestAggregate aggregate = aggregateService.get(stream, TestAggregate.class);

        stream.append(createEventAndApply(SNAPSHOT_THRESHOLD - 2, "context.eventA", aggregate));

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, IsNot.not(Matchers.nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(48));

        TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(25));
    }

    @Test
    public void shouldStoreANewSnapshotOnTopOfExistingSnapshot() throws Exception {

        final long snapshotCount = snapshotRepository.snapshotCount(STREAM_ID);
        assertThat(snapshotCount, is(0L));

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        rebuildAggregateAndApplyEvents(eventStream, SNAPSHOT_THRESHOLD);

        rebuildAggregateAndApplyEvents(eventStream, SNAPSHOT_THRESHOLD);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, IsNot.not(Matchers.nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(50L));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(2L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(50));

        TestAggregate aggregateFromSnapshot = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregateFromSnapshot.numberOfAppliedEvents(), is(50));
    }

    @Test
    public void shouldRebuildSnapshotOnAggregateModelChange() throws Exception {

        final EventStream eventStream = eventSource.getStreamById(STREAM_ID);

        DynamicAggregateTestClassGenerator classGenerator = new DynamicAggregateTestClassGenerator();

        final Class oldAggregateClass = classGenerator.generatedTestAggregateClassOf(1L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        final long initialNumberOfSnapshots = 4;

        for (int i = 1; i <= initialNumberOfSnapshots; i++) {
            eventStream.append(createEventStreamAndApply(SNAPSHOT_THRESHOLD, "context.eventA", aggregateService.get(eventStream, oldAggregateClass)));
        }


        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, oldAggregateClass);

        assertThat(snapshot, IsNot.not(Matchers.nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(4L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(100));


        final Class newAggregateClass = classGenerator.generatedTestAggregateClassOf(2L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        snapshotService.setStreamStrategy(
                new CustomClassLoaderObjectInputStreamStrategy(classLoaderWithGeneratedAggregateLoaded()));

        eventStream.append(createEventStreamAndApply(SNAPSHOT_THRESHOLD - 2, "context.eventA", aggregateService.get(eventStream, newAggregateClass)));

        final Optional<AggregateSnapshot> newSnapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, newAggregateClass);
        assertThat(newSnapshot, IsNot.not(Matchers.nullValue()));
        assertThat(newSnapshot.isPresent(), equalTo(true));
        assertThat(newSnapshot.get().getType(), equalTo(newAggregateClass.getName()));
        assertThat(newSnapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(newSnapshot.get().getVersionId(), equalTo(123L));
        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
        assertThat(eventLogRepository.eventLogCount(STREAM_ID), is(123));
    }

    private void initEventDatabase() throws Exception {

        Liquibase eventStoreLiquibase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        eventStoreLiquibase.dropAll();
        eventStoreLiquibase.update("");
        Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_SNAPSHOT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        snapshotLiquidBase.update("");

    }

    private DynamicallyLoadingClassLoader classLoaderWithGeneratedAggregateLoaded() throws ClassNotFoundException {

        final DynamicallyLoadingClassLoader classLoader = new DynamicallyLoadingClassLoader(this.getClass(), TEST_AGGREGATE_CLASS_NAME, TEST_AGGREGATE_COMPILED_CLASS);
        classLoader.loadClass(AGGREGATE_INTERFACE_FULL_NAME);
        classLoader.loadClass(TEST_AGGREGATE_FULL_NAME);

        return classLoader;
    }

    private <T extends Aggregate> void rebuildAggregateAndApplyEvents(final EventStream eventStream, long eventCount) throws Exception {

        TestAggregate aggregateRebuilt = aggregateService.get(eventStream, TestAggregate.class);

        eventStream.append(createEventAndApply(eventCount, "context.eventA", aggregateRebuilt));
    }

    private Stream<JsonEnvelope> createEventAndApply(long count, String eventName, TestAggregate aggregate) {
        List<Object> envelopes = new LinkedList<>();
        for (int i = 1; i <= count; i++) {
            JsonEnvelope envelope =
                    envelope()
                    .with(metadataWithRandomUUID(eventName)
                            .createdAt(clock.now())
                            .withStreamId(STREAM_ID))
                    .withPayloadOf("value", "name")
                    .build();
            aggregate.addEvent(envelope);
            envelopes.add(envelope);
        }
        return envelopes.stream().map(x -> (JsonEnvelope) x);
    }

    private <T extends Aggregate> Stream<JsonEnvelope> createEventStreamAndApply(long count, String eventName, T aggregate) {
        List<Object> envelopes = new LinkedList<>();

        for (int i = 1; i <= count; i++) {

            JsonEnvelope envelope =
                    envelope()
                    .with(metadataWithRandomUUID(eventName)
                            .createdAt(clock.now())
                            .withStreamId(STREAM_ID))
                    .withPayloadOf("value", "name")
                    .build();

            aggregate.apply(new EventA(String.valueOf(i)));
            envelopes.add(envelope);
        }
        return envelopes.stream().map(x -> (JsonEnvelope) x);
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
    public static class TestServiceContextNameProvider implements ServiceContextNameProvider {

        @Override
        public String getServiceContextName() {
            return "test-component";
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