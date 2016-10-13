package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import static java.util.UUID.randomUUID;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotJdbcRepository;
import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SnapshotRepositoryJdbcIT extends AbstractJdbcRepositoryIT<SnapshotJdbcRepository> {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final UUID STREAM_ID = randomUUID();

    private static final Long VERSION_ID = 5L;
    private static final Class<RecordingAggregate> TYPE = RecordingAggregate.class;
    private static final byte[] AGGREGATE = "Any String you want".getBytes();
    private static final String LIQUIBASE_SNAPSHOT_STORE_DB_CHANGELOG_XML = "liquibase/snapshot-store-db-changelog.xml";

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

        AggregateSnapshot aggregateSnapshot = createSnapshot(STREAM_ID, VERSION_ID, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot);

        Optional<AggregateSnapshot<RecordingAggregate>> snapshot = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        Assert.assertThat(snapshot, CoreMatchers.notNullValue());
        Assert.assertThat(snapshot, CoreMatchers.is(Optional.of(aggregateSnapshot)));
    }

    @Test
    public void shouldRetrieveLatestSnapshot() {

        AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID + 1, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot2 = createSnapshot(STREAM_ID, VERSION_ID + 2, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot3 = createSnapshot(STREAM_ID, VERSION_ID + 3, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot4 = createSnapshot(STREAM_ID, VERSION_ID + 4, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot5 = createSnapshot(STREAM_ID, VERSION_ID + 5, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot1);
        jdbcRepository.storeSnapshot(aggregateSnapshot2);
        jdbcRepository.storeSnapshot(aggregateSnapshot3);
        jdbcRepository.storeSnapshot(aggregateSnapshot4);
        jdbcRepository.storeSnapshot(aggregateSnapshot5);

        Optional<AggregateSnapshot<RecordingAggregate>> snapshot = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        Assert.assertThat(snapshot, CoreMatchers.notNullValue());
        Assert.assertThat(snapshot, CoreMatchers.is(Optional.of(aggregateSnapshot5)));
    }

    @Test
    public void shouldRemoveAllSnapshots() {

        AggregateSnapshot aggregateSnapshot1 = createSnapshot(STREAM_ID, VERSION_ID + 1, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot2 = createSnapshot(STREAM_ID, VERSION_ID + 2, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot3 = createSnapshot(STREAM_ID, VERSION_ID + 3, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot4 = createSnapshot(STREAM_ID, VERSION_ID + 4, TYPE, AGGREGATE);
        AggregateSnapshot aggregateSnapshot5 = createSnapshot(STREAM_ID, VERSION_ID + 6, TYPE, AGGREGATE);

        jdbcRepository.storeSnapshot(aggregateSnapshot1);
        jdbcRepository.storeSnapshot(aggregateSnapshot2);
        jdbcRepository.storeSnapshot(aggregateSnapshot3);
        jdbcRepository.storeSnapshot(aggregateSnapshot4);
        jdbcRepository.storeSnapshot(aggregateSnapshot5);

        jdbcRepository.removeAllSnapshots(STREAM_ID, TYPE);

        Optional<AggregateSnapshot<RecordingAggregate>> snapshots = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        Assert.assertThat(snapshots, CoreMatchers.notNullValue());
        Assert.assertThat(snapshots.isPresent(), CoreMatchers.is(false));
    }


    @Test
    public void shouldReturnOptionalNullIfNoSnapshotAvailable() {
        Optional<AggregateSnapshot<RecordingAggregate>> snapshot = jdbcRepository.getLatestSnapshot(STREAM_ID, TYPE);

        Assert.assertThat(snapshot.isPresent(), CoreMatchers.is(false));
    }

    private <T extends Aggregate> AggregateSnapshot createSnapshot(final UUID streamId, final Long sequenceId, Class<T> type, byte[] aggregate) {

        return new AggregateSnapshot(streamId, sequenceId, type, aggregate);
    }

    public class RecordingAggregate implements Aggregate {

        List<Object> recordedEvents = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            recordedEvents.add(event);
            return event;
        }
    }
}


