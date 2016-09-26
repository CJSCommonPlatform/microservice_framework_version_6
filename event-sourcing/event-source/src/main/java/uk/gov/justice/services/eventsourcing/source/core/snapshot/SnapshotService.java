package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;

import java.util.UUID;


/**
 * The interface Snapshot service.
 */
public interface SnapshotService {

    /**
     * Attempt aggregate store.
     *
     * @param <T>                    the type parameter
     * @param streamId               the stream id
     * @param streamVersionId        the stream version id
     * @param aggregate              the aggregate
     * @param currentSnapshotVersion the current snapshot version
     */
    public <T extends Aggregate> void attemptAggregateStore(final UUID streamId,
                                                            final long streamVersionId,
                                                            final T aggregate,
                                                            final long currentSnapshotVersion);

    /**
     * Gets latest versioned aggregate.
     *
     * @param <T>            the type parameter
     * @param streamId       the stream id
     * @param clazz          the clazz
     * @param streamStrategy the stream strategy
     * @return the latest versioned aggregate
     * @throws AggregateChangeDetectedException the aggregate change detected exception
     */
    public <T extends Aggregate> VersionedAggregate<T> getLatestVersionedAggregate(final UUID streamId,
                                                                                   final Class<T> clazz,
                                                                                   final ObjectInputStreamStrategy streamStrategy)
            throws AggregateChangeDetectedException;

    /**
     * Rebuild aggregate versioned aggregate.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the versioned aggregate
     */
    public <T extends Aggregate> VersionedAggregate<T> rebuildAggregate(final UUID streamId, final Class<T> clazz);

    /**
     * Gets new versioned aggregate.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the new versioned aggregate
     */
    public <T extends Aggregate> VersionedAggregate<T> getNewVersionedAggregate(final Class<T> clazz);
}
