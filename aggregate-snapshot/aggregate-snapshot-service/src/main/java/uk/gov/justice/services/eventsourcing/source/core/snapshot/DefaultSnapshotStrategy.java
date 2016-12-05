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
    @Value(key = "snapshotThreshold", defaultValue = "25")
    long snapshotThreshold;

    @Override
    public boolean shouldCreateSnapshot(final long aggregateVersionId, final long snapshotVersionId) {
        logger.trace("Checking snapshot Strategy for if we can create a snapshot {}", aggregateVersionId, snapshotVersionId);
        return (aggregateVersionId - snapshotVersionId) >= snapshotThreshold;
    }


}