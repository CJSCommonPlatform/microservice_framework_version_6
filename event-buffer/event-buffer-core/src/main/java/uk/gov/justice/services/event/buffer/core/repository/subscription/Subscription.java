package uk.gov.justice.services.event.buffer.core.repository.subscription;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Entity to represent event subscription
 */
public class Subscription {

    private final UUID streamId;
    private final long position;
    private final String source;

    public Subscription(final UUID streamId, final long position, final String source) {
        this.streamId = streamId;
        this.position = position;
        this.source = source;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getPosition() {
        return position;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "streamId=" + streamId +
                ", position=" + position +
                ", source='" + source + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(position, that.position)
                .append(source, that.source)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(position)
                .append(source)
                .toHashCode();
    }
}
