package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;

public class VersionedAggregate <T extends Aggregate> {

    private final long versionId;
    private final T aggregate;

    public VersionedAggregate(final long version, final T aggregate) {
        this.aggregate = aggregate;
        this.versionId = version;
    }

    public T getAggregate() {
        return aggregate;
    }

    public long getVersionId() {
        return versionId;
    }


}
