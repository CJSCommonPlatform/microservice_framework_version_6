package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
@Alternative
@Priority(1)
public class DefaultSnapshotStrategy implements SnapshotStrategy {

    @Inject
    Logger logger;

    private final static int SNAPSHOT_THRESHOLD = 25;

    @Override
    public boolean shouldCreateSnapshot(final long aggregateVersionId,
                                        final long snapshotVersionId) {
        logger.trace("Checking snapshot Strategy for if we can create a snapshot {}", aggregateVersionId, snapshotVersionId);
        if ((aggregateVersionId - snapshotVersionId) >= SNAPSHOT_THRESHOLD) {
            return true;
        }
        return false;
    }
}