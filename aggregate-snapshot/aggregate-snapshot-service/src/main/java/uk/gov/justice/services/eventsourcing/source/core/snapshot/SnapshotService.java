package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;

import java.util.Optional;
import java.util.UUID;


/**
 * The interface Snapshot service.
 */
public interface SnapshotService {

    /**
     * Attempt aggregate store.
     *  @param <T>                    the type parameter
     * @param streamId               the stream id
     * @param streamVersionId        the stream version id
     * @param aggregate              the aggregate
     */
    public <T extends Aggregate> void attemptAggregateStore(final UUID streamId,
                                                            final long streamVersionId,
                                                            final T aggregate);

    /**
     * Gets latest versioned aggregate.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest versioned aggregate
     * @throws AggregateChangeDetectedException the aggregate change detected exception
     */
    public <T extends Aggregate> Optional<VersionedAggregate<T>> getLatestVersionedAggregate(final UUID streamId,
                                                                                             final Class<T> clazz)
            throws AggregateChangeDetectedException;

    /**
     * Remove all snapshots.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     */
    public <T extends Aggregate> void removeAllSnapshots(final UUID streamId, final Class<T> clazz);

    /**
     * Gets latest snapshot version.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest snapshot version
     */
    public <T extends Aggregate> long getLatestSnapshotVersion(final UUID streamId,
                                                               final Class<T> clazz);

}
