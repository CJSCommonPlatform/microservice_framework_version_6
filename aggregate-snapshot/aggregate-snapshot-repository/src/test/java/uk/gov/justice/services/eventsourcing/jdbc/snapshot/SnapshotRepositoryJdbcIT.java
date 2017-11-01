package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SnapshotRepositoryJdbcIT extends AbstractJdbcRepositoryIT<SnapshotJdbcRepository> {

    private static final UUID STREAM_ID = randomUUID();
    private static final Long VERSION_ID = 5L;
    private static final Class<RecordingAggregate> TYPE = RecordingAggregate.class;
    private static final Class<DifferentAggregate> OTHER_TYPE = DifferentAggregate.class;
    private static final byte[] AGGREGATE = "Any String you want".getBytes();
    private static final String LIQUIBASE_SNAPSHOT_STORE_DB_CHANGELOG_XML = "liquibase/snapshot-store-db-changelog.xml";

    private final Poller poller = new Poller(10, 1000L);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public SnapshotRepositoryJdbcIT() {
        super(LIQUIBASE_SNAPSHOT_STORE_DB_CHANGELOG_XML);
    }

    @Before
    public void initializeDependencies() throws Exception {
        jdbcRepository = new SnapshotJdbcRepository();
        registerDataSource();
    }

    @Test
    public void shouldStoreAndRetrieveSnapshot() {

        final AggregateSnapshot aggregateSnapshot = createSnapshot(STREAM_ID, VERSION_ID, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot)));
    }

    @Test
    public void shouldRetrieveLatestSnapshot() {

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(STREAM_ID, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(STREAM_ID, VERSION_ID + 3, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot4 = createSnapshot(STREAM_ID, VERSION_ID + 4, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot5 = createSnapshot(STREAM_ID, VERSION_ID + 5, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot1);
        jdbcRepository.storeSnapshot(aggregateSnapshot2);
        jdbcRepository.storeSnapshot(aggregateSnapshot3);
        jdbcRepository.storeSnapshot(aggregateSnapshot4);
        jdbcRepository.storeSnapshot(aggregateSnapshot5);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot5)));
    }

    @Ignore("Ignoring until fix from master is merged into this branch")
    @Test
    public void shouldRetrieveLatestSnapshotWithCorrectType() {

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(STREAM_ID, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(STREAM_ID, VERSION_ID + 3, OTHER_TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot1);
        jdbcRepository.storeSnapshot(aggregateSnapshot2);
        jdbcRepository.storeSnapshot(aggregateSnapshot3);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot  = poller.pollUntilFound(() -> jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE));

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot2)));
    }

    @Test
    public void shouldRemoveAllSnapshots() {

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(STREAM_ID, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(STREAM_ID, VERSION_ID + 3, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot4 = createSnapshot(STREAM_ID, VERSION_ID + 4, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot5 = createSnapshot(STREAM_ID, VERSION_ID + 6, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot1);
        jdbcRepository.storeSnapshot(aggregateSnapshot2);
        jdbcRepository.storeSnapshot(aggregateSnapshot3);
        jdbcRepository.storeSnapshot(aggregateSnapshot4);
        jdbcRepository.storeSnapshot(aggregateSnapshot5);

        jdbcRepository.removeAllSnapshots(STREAM_ID, TYPE);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshots = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        assertThat(snapshots, notNullValue());
        assertThat(snapshots.isPresent(), is(false));
    }


    @Ignore("Ignoring until fix from master is merged into this branch")
    @Test
    public void shouldReturnOptionalNullIfNoSnapshotAvailable() {
        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot  = poller.pollUntilFound(() -> jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE));

        assertThat(snapshot.isPresent(), is(false));
    }

    @Ignore("Ignoring until fix from master is merged into this branch")
    @Test
    public void shouldRetrieveOptionalNullIfOnlySnapshotsOfDifferentTypesAvailable() {
        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID, OTHER_TYPE, AGGREGATE);
        jdbcRepository.storeSnapshot(aggregateSnapshot1);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot  = poller.pollUntilFound(() -> jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE));

        assertThat(snapshot.isPresent(), is(false));

    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> AggregateSnapshot createSnapshot(final UUID streamId, final Long sequenceId, Class<T> type, byte[] aggregate) {
        return new AggregateSnapshot(streamId, sequenceId, type, aggregate);
    }

    public class RecordingAggregate implements Aggregate {
        final List<Object> recordedEvents = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            recordedEvents.add(event);
            return event;
        }
    }

    public class DifferentAggregate implements Aggregate {
        @Override
        public Object apply(Object event) {
            return null;
        }
    }
}
