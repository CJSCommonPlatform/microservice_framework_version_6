package uk.gov.justice.services.core.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * The type Snapshot aware aggregate service.
 */
@ApplicationScoped
@Alternative
@Priority(100)
public class SnapshotAwareAggregateService implements AggregateService {

    @Inject
    Logger logger;

    @Inject
    SnapshotService snapshotService;

    @Inject
    DefaultAggregateService defaultAggregateService;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Aggregate> T get(final EventStream stream, final Class<T> clazz) {
        logger.trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", stream.getId(), clazz);

        final Optional<VersionedAggregate<T>> versionedAggregate = latestOrChangedAggregateAndEvents(stream, clazz);

        final T aggregate = aggregateOf(stream, clazz, versionedAggregate);

        if (stream instanceof SnapshotAwareEnvelopeEventStream) {
            ((SnapshotAwareEnvelopeEventStream) stream).registerAggregates(clazz, aggregate);
        }

        return aggregate;
    }

    private <T extends Aggregate> T aggregateOf(final EventStream stream, Class<T> clazz, final Optional<VersionedAggregate<T>> versionedAggregate) {
        if (versionedAggregate.isPresent()) {
            final VersionedAggregate<T> versionedAggregateValue = versionedAggregate.get();
            return defaultAggregateService.applyEvents(
                    stream.readFrom(versionAfter(versionedAggregateValue)),
                    versionedAggregateValue.getAggregate());
        }

        return defaultAggregateService.get(stream, clazz);
    }

    private <T extends Aggregate> long versionAfter(final VersionedAggregate<T> versionedAggregate) {
        return versionedAggregate.getVersionId() + 1L;
    }

    private <T extends Aggregate> Optional<VersionedAggregate<T>> latestOrChangedAggregateAndEvents(final EventStream stream, final Class<T> clazz) {
        try {
            return snapshotService.getLatestVersionedAggregate(stream.getId(), clazz);
        } catch (AggregateChangeDetectedException e) {
            snapshotService.removeAllSnapshots(stream.getId(), clazz);
            return Optional.empty();
        }
    }
}
