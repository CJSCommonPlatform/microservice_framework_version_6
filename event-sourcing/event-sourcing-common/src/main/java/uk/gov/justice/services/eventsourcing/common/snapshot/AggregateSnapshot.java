package uk.gov.justice.services.eventsourcing.common.snapshot;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AggregateSnapshot {

    private final UUID streamId;
    private final Long sequenceId;
    private final String type;
    private final byte[] aggregate;

    public AggregateSnapshot(UUID streamId, Long sequenceId, String type, byte[] aggregate) {
        this.streamId = streamId;
        this.sequenceId = sequenceId;
        this.type = type;
        this.aggregate = aggregate;
    }


    public UUID getStreamId() {
        return streamId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public String getType() {
        return type;
    }

    public byte[] getAggregate() {
        return aggregate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AggregateSnapshot that = (AggregateSnapshot) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(sequenceId, that.sequenceId)
                .append(type, that.type)
                .append(aggregate, that.aggregate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(sequenceId)
                .append(type)
                .append(aggregate)
                .toHashCode();
    }
}
