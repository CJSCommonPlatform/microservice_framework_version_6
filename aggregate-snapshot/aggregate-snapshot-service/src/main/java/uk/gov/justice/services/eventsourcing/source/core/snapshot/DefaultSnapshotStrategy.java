package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import javax.inject.Inject;

import org.slf4j.Logger;

public class DefaultSnapshotStrategy implements SnapshotStrategy {

    private final static int SNAPSHOT_THRESHOLD = 25;

    @Inject
    Logger logger;

    @Override
    public boolean shouldCreateSnapshot(final long aggregateVersionId, final long snapshotVersionId) {
        logger.trace("Checking snapshot Strategy for if we can create a snapshot {}", aggregateVersionId, snapshotVersionId);
        return (aggregateVersionId - snapshotVersionId) >= SNAPSHOT_THRESHOLD;
    }
}