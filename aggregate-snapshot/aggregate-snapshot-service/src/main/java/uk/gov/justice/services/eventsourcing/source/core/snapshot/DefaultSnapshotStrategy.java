package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.services.common.configuration.Value;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class DefaultSnapshotStrategy implements SnapshotStrategy {

    @Inject
    Logger logger;

    @Inject
    @Value(key = "default.strategy.snapshot.threshold", defaultValue = "25")
    long snapshotThreshold;

    @Override
    public boolean shouldCreateSnapshot(final long aggregateVersionId, final long snapshotVersionId) {
        logger.trace("Checking if we should create a snapshot for aggregate version: {}, current snapshot version: {}, threshold: {}", aggregateVersionId, snapshotVersionId, snapshotThreshold);
        return (aggregateVersionId - snapshotVersionId) >= snapshotThreshold;
    }


}