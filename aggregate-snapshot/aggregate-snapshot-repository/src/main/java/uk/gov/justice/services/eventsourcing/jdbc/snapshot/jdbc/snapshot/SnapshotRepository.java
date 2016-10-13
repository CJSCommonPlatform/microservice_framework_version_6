package uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot;


import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;

import java.util.Optional;
import java.util.UUID;

/**
 * The interface Snapshot repository.
 */
public interface SnapshotRepository {

    /**
     * Store snapshot.
     *
     * @param AggregateSnapshot the aggregate snapshot
     */
    void storeSnapshot(final AggregateSnapshot AggregateSnapshot);

    /**
     * Gets latest snapshot.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest snapshot
     */
    <T extends Aggregate> Optional<AggregateSnapshot<T>> getLatestSnapshot(final UUID streamId, final Class<T> clazz);


    /**
     * Remove all snapshots.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     */
    <T extends Aggregate> void removeAllSnapshots(final UUID streamId, final Class<T> clazz);

    /**
     * Gets latest snapshot version.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest snapshot version
     */
    <T extends Aggregate> long getLatestSnapshotVersion(final UUID streamId, final Class<T> clazz);
}
