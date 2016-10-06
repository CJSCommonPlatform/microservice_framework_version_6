package uk.gov.justice.services.core.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.SnapshotAwareEnvelopeEventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.VersionedAggregate;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * The type Snapshot aware aggregate service.
 */
@ApplicationScoped
public class SnapshotAwareAggregateService implements AggregateService {

    @Inject
    Logger logger;

    @Inject
    SnapshotService snapshotService;

    @Inject
    DefaultAggregateService defaultAggregateService;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Override
    public <T extends Aggregate> T get(final EventStream stream, final Class<T> clazz) {
        logger.trace("SnapshotAwareAggregateService Recreating aggregate for instance {} of aggregate type {}", stream.getId(), clazz);

        final Optional<VersionedAggregate<T>> versionedAggregate = latestOrChangedAggregateAndEvents(stream, clazz);

        T aggregate = aggregateOf(stream, clazz, versionedAggregate);

        if (stream instanceof SnapshotAwareEnvelopeEventStream) {
            ((SnapshotAwareEnvelopeEventStream) stream).registerAggregates(clazz, aggregate);
        }

        return aggregate;
    }

    protected <T extends Aggregate> T aggregateOf(EventStream stream, Class<T> clazz, Optional<VersionedAggregate<T>> versionedAggregate) {
        if (versionedAggregate.isPresent()) {
            return defaultAggregateService.applyEvents(stream.readFrom(versionedAggregate.get().getVersionId() + 1), versionedAggregate.get().getAggregate());
        }
        return defaultAggregateService.get(stream, clazz);

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