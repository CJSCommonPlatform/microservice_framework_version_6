package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.justice.services.core.aggregate.util.DynamicAggregateTestClassGenerator.generatedTestAggregateClassOf;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.DateTimeProvider;
import uk.gov.justice.services.core.aggregate.util.CustomClassLoaderObjectInputStreamStrategy;
import uk.gov.justice.services.core.aggregate.util.DynamicallyLoadingClassLoader;
import uk.gov.justice.services.core.aggregate.util.EventLogOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.core.aggregate.util.SnapshotOpenEjbAwareJdbcRepository;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.repository.core.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog.EventLogConverter;
import uk.gov.justice.services.eventsourcing.source.core.EnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
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
public class AggregateServiceIT {

    private static final UUID STREAM_ID = randomUUID();

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";

    private static final String AGGREGATE_INTERFACE_FULL_NAME = "uk.gov.justice.domain.aggregate.Aggregate";

    private static final String TEST_AGGREGATE_CLASS_NAME = "GeneratedTestAggregate";

    private static final String TEST_AGGREGATE_PACKAGE = "uk.gov.justice.services.core.aggregate";

    private static final String TEST_AGGREGATE_FULL_NAME = format("%s.%s", TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

    private static final String TEST_AGGREGATE_COMPILED_CLASS = format("%s/%s.class", TEST_AGGREGATE_PACKAGE.replace(".", "/"), TEST_AGGREGATE_CLASS_NAME);

    private static final String TYPE = TEST_AGGREGATE_PACKAGE+".AggregateServiceIT$TestAggregate";

    private static final int SNAPSHOT_THRESHOLD = 25;

    @Resource(name = "openejb/Resource/eventStore")
    private DataSource dataSource;

    @Inject
    private SnapshotOpenEjbAwareJdbcRepository snapshotRepository;

    @Inject
    private EventSource eventSource;

    @Inject
    private AggregateService aggregateService;

    @Module
    @Classes(cdi = true, value = {
            CustomClassLoaderObjectInputStreamStrategy.class,
            AggregateService.class,
            SnapshotOpenEjbAwareJdbcRepository.class,
            EventLogOpenEjbAwareJdbcRepository.class,
            EventSource.class,
            EnvelopeEventStream.class,
            EventStreamManager.class,
            LoggerProducer.class,
            EventRepository.class,
            EventLogConverter.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            JsonObjectToObjectConverter.class,
            ObjectMapperProducer.class,
            JdbcEventRepository.class,
            JmsEventPublisher.class,
            DummyJmsEnvelopeSender.class,
            DefaultEventDestinationResolver.class,
            EnvelopeConverter.class,
            DefaultSnapshotService.class,
            DefaultSnapshotStrategy.class,
            DateTimeProvider.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("snapshot-test")
                .addServlet("SnapShotApp", Application.class.getName());
    }

    @Before
    public void init() throws Exception {
        initDatabase();
        aggregateService.register(new EventFoundEvent(EventA.class, "context.eventA"));
    }

    @Test
    public void shouldStoreABrandNewSnapshotWhenEventCountInTheStreamReachesThreshold() throws Exception {


        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        stream.append(envelopes(SNAPSHOT_THRESHOLD));
        aggregateService.get(stream, TestAggregate.class);

        final  Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));

        final TestAggregate aggregate = snapshot.get().getAggregate(new DefaultObjectInputStreamStrategy());
        assertThat(aggregate.numberOfAppliedEvents, is(SNAPSHOT_THRESHOLD));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
    }

    @Test
    public void shouldCreateNewSnapshotOnAggregateChangeWhenWeJustOneExistingSnapshots() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        final Class oldClass = generatedTestAggregateClassOf(1L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        stream.append(envelopes(SNAPSHOT_THRESHOLD));
        aggregateService.get(stream, oldClass);

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, oldClass);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));

        stream.append(envelopes(SNAPSHOT_THRESHOLD - 2));


        final Class clazzNew = generatedTestAggregateClassOf(2L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        final ObjectInputStreamStrategy streamStrategy = new CustomClassLoaderObjectInputStreamStrategy(classLoaderWithGeneratedAggregateLoaded());

        aggregateService.setObjectInputStreamStrategy(streamStrategy);
        aggregateService.get(stream, clazzNew);

        final Optional<AggregateSnapshot> snapshotChanged = snapshotRepository.getLatestSnapshot(STREAM_ID, clazzNew);
        assertThat(snapshotChanged, not(nullValue()));
        assertThat(snapshotChanged.isPresent(), equalTo(true));
        assertThat(snapshotChanged.get().getType(), equalTo(clazzNew.getName()));
        assertThat(snapshotChanged.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshotChanged.get().getVersionId(), equalTo(48L));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
    }

    @Test
    public void shouldCreateNewSnapshotOnAggregateChangeWhenWeHaveMultipleExistingSnapshots() throws Exception {

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        final Class oldAggregateClass = generatedTestAggregateClassOf(1L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        final long initialNumberOfSnapshots = 4;
        for (int i = 0; i < initialNumberOfSnapshots; i++) {
            triggerSnapshotGeneration(stream, oldAggregateClass);
        }

        final Optional<AggregateSnapshot> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, oldAggregateClass);

        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(initialNumberOfSnapshots));

        final Class newAggregateClass = generatedTestAggregateClassOf(2L, TEST_AGGREGATE_PACKAGE, TEST_AGGREGATE_CLASS_NAME);

        aggregateService.setObjectInputStreamStrategy(
                new CustomClassLoaderObjectInputStreamStrategy(classLoaderWithGeneratedAggregateLoaded()));

        aggregateService.get(stream, newAggregateClass);

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));

        final Optional<AggregateSnapshot> newSnapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, newAggregateClass);
        assertThat(newSnapshot, not(nullValue()));
        assertThat(newSnapshot.isPresent(), equalTo(true));
        assertThat(newSnapshot.get().getType(), equalTo(newAggregateClass.getName()));
        assertThat(newSnapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(newSnapshot.get().getVersionId(), equalTo(initialNumberOfSnapshots * SNAPSHOT_THRESHOLD));

    }

    @Test
    public void shouldNotStoreABrandNewSnapshotWhenStrategyDoesNotMandateSavingSnapshot() throws Exception {

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));

        final EventStream stream = eventSource.getStreamById(STREAM_ID);
        stream.append(envelopes(SNAPSHOT_THRESHOLD - 1));

        aggregateService.get(stream, TestAggregate.class);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(false));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(0L));
    }

    @Test
    public void shouldNotStoreANewSnapshotOnTopOfExistingSnapshotsWhenThresholdNotMet() throws Exception {


        final  EventStream stream = eventSource.getStreamById(STREAM_ID);

        triggerSnapshotGeneration(stream, TestAggregate.class);

        stream.append(envelopes(SNAPSHOT_THRESHOLD - 2));

        aggregateService.get(stream, TestAggregate.class);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(25L));


        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(1L));
    }

    @Test
    public void shouldStoreANewSnapshotOnTopOfExistingSnapshot() throws Exception {

        final long snapshotCount = snapshotRepository.snapshotCount(STREAM_ID);
        assertThat(snapshotCount, is(0L));

        final EventStream stream = eventSource.getStreamById(STREAM_ID);

        triggerSnapshotGeneration(stream, TestAggregate.class);

        triggerSnapshotGeneration(stream, TestAggregate.class);

        final Optional<AggregateSnapshot<TestAggregate>> snapshot = snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class);
        assertThat(snapshot, not(nullValue()));
        assertThat(snapshot.isPresent(), equalTo(true));
        assertThat(snapshot.get().getType(), equalTo(TYPE));
        assertThat(snapshot.get().getStreamId(), equalTo(STREAM_ID));
        assertThat(snapshot.get().getVersionId(), equalTo(50L));

        assertThat(snapshotRepository.snapshotCount(STREAM_ID), is(2L));
    }

    private void initDatabase() throws Exception {

        Liquibase snapshotLiquidBase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(dataSource.getConnection()));
        snapshotLiquidBase.dropAll();
        snapshotLiquidBase.update("");

    }

    private DynamicallyLoadingClassLoader classLoaderWithGeneratedAggregateLoaded() throws ClassNotFoundException {
        final DynamicallyLoadingClassLoader classLoader = new DynamicallyLoadingClassLoader(this.getClass(), TEST_AGGREGATE_CLASS_NAME, TEST_AGGREGATE_COMPILED_CLASS);
        classLoader.loadClass(AGGREGATE_INTERFACE_FULL_NAME);
        classLoader.loadClass(TEST_AGGREGATE_FULL_NAME);
        return classLoader;
    }

    private void triggerSnapshotGeneration(final EventStream eventStream, final Class aggregateClass) throws Exception {
        eventStream.append(envelopes(SNAPSHOT_THRESHOLD));
        aggregateService.get(eventStream, aggregateClass);
    }

    private Stream<JsonEnvelope> envelopes(final int numberOfEnvelopes) {
        List<JsonEnvelope> envelopes = new LinkedList<>();
        for (int i = 1; i <= numberOfEnvelopes; i++) {
            envelopes.add(envelope().with(metadataWithRandomUUID("context.eventA").withStreamId(STREAM_ID)).withPayloadOf("value", "name").build());
        }
        return envelopes.stream();
    }

    @Event("eventA")
    public static class EventA {

        private String name;

        public EventA() {

        }

        public String getName() {
            return name;
        }

    }

    public static class TestAggregate implements Aggregate {
        private static final long serialVersionUID = 42L;
        private int numberOfAppliedEvents = 0;

        @Override
        public Object apply(Object event) {
            numberOfAppliedEvents++;
            return event;
        }

        public int numberOfAppliedEvents() {
            return numberOfAppliedEvents;
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