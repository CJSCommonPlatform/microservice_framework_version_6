package uk.gov.justice.services.eventsourcing.source.core.snapshot;

/**
 * The interface Snapshot strategy.
 */
public interface SnapshotStrategy {

    /**
     * Should create snapshot boolean.
     *
     * @param aggregateVersionId the aggregate version id
     * @param snapshotVersionId  the snapshot version id
     * @return the boolean
     */
    boolean shouldCreateSnapshot(final long aggregateVersionId, final long snapshotVersionId);
}
