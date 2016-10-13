package uk.gov.justice.domain.snapshot;

import static org.apache.commons.lang.SerializationUtils.serialize;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AggregateSnapshot<T extends Aggregate> implements Serializable {

    private final UUID streamId;
    private final Long versionId;
    private final String type;
    private final byte[] aggregateByteRepresentation;

    @SuppressWarnings("unchecked")
    public AggregateSnapshot(final UUID streamId, final Long versionId, final T aggregate) {
        this(streamId, versionId, (Class<T>) aggregate.getClass(), serialize(aggregate));
    }

    public AggregateSnapshot(final UUID streamId, final Long versionId, final Class<T> type, final byte[] aggregateByteRepresentation) {
        this.streamId = streamId;
        this.versionId = versionId;
        this.type = type.getName();
        this.aggregateByteRepresentation = aggregateByteRepresentation;
    }

    public AggregateSnapshot(final UUID streamId, final Long versionId, final String type, final byte[] aggregateByteRepresentation) {
        this.streamId = streamId;
        this.versionId = versionId;
        this.type = type;
        this.aggregateByteRepresentation = aggregateByteRepresentation;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public String getType() {
        return type;
    }

    public byte[] getAggregateByteRepresentation() {
        return aggregateByteRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AggregateSnapshot that = (AggregateSnapshot) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(versionId, that.versionId)
                .append(type, that.type)
                .append(aggregateByteRepresentation, that.aggregateByteRepresentation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(versionId)
                .append(type)
                .append(aggregateByteRepresentation)
                .toHashCode();
    }

    public T getAggregate(final ObjectInputStreamStrategy streamStrategy) throws AggregateChangeDetectedException {
        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(aggregateByteRepresentation);
            final ObjectInputStream ois = streamStrategy.objectInputStreamOf(bis);
            final Object object = ois.readObject();
            ois.close();
            return (T) Class.forName(getType()).cast(object);
        } catch (SerializationException | ClassNotFoundException | IOException e) {
            throw new AggregateChangeDetectedException(e.getLocalizedMessage());
        }
    }

}
