package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.jdbc.snapshot.SnapshotRepository;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.SerializationException;
import org.slf4j.Logger;

/**
 * The type Default snapshot service.
 */
@ApplicationScoped
public class DefaultSnapshotService implements SnapshotService {

    @Inject
    Logger logger;

    @Inject
    SnapshotRepository snapshotRepository;

    @Inject
    SnapshotStrategy snapshotStrategy;

    private ObjectInputStreamStrategy streamStrategy = new DefaultObjectInputStreamStrategy();

    public void setStreamStrategy(ObjectInputStreamStrategy streamStrategy) {
        this.streamStrategy = streamStrategy;
    }

    @Override
    public <T extends Aggregate> void attemptAggregateStore(final UUID streamId, final long streamVersionId, final T aggregate) {
        final long currentSnapshotVersion = snapshotRepository.getLatestSnapshotVersion(streamId, aggregate.getClass());
        if (snapshotStrategy.shouldCreateSnapshot(streamVersionId, currentSnapshotVersion)) {
            try {
                logger.trace("Storing snapshot of aggregate: {}, streamId: {}, version: {}", aggregate.getClass().getSimpleName(), streamId, streamVersionId);
                snapshotRepository.storeSnapshot(new AggregateSnapshot<>(streamId, streamVersionId, aggregate));
            } catch (SerializationException e) {
                logger.error("Error creating snapshot for {}", streamId, e);
            }
        }
    }

    @Override
    public <T extends Aggregate> Optional<VersionedAggregate<T>> getLatestVersionedAggregate(final UUID streamId, final Class<T> clazz)
            throws AggregateChangeDetectedException {
        logger.trace("Retrieving snapshot for stream id: {}, aggregate: {}", streamId, clazz.getSimpleName());

        final Optional<AggregateSnapshot<T>> aggregateSnapshot = snapshotRepository.getLatestSnapshot(streamId, clazz);

        if (aggregateSnapshot.isPresent()) {
            final AggregateSnapshot<T> snapshotValue = aggregateSnapshot.get();
            final VersionedAggregate<T> versionedAggregate = new VersionedAggregate<T>(snapshotValue.getVersionId(), snapshotValue.getAggregate(streamStrategy));
            return Optional.of(versionedAggregate);
        }

        return Optional.empty();
    }

    @Override
    public <T extends Aggregate> void removeAllSnapshots(final UUID streamId, final Class<T> clazz) {
        logger.trace("Removing all snapshots for {}", streamId, clazz);
        snapshotRepository.removeAllSnapshots(streamId, clazz);
    }

    @Override
    public <T extends Aggregate> long getLatestSnapshotVersion(final UUID streamId, final Class<T> clazz) {
        logger.trace("Getting the latest snapshot version for {}", clazz.getSimpleName());
        return snapshotRepository.getLatestSnapshotVersion(streamId, clazz);
    }
}

