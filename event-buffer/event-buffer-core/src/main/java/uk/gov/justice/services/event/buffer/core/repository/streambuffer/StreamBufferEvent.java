package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.lang.Math.toIntExact;


public class StreamBufferEvent implements Comparable<StreamBufferEvent> {
    private UUID streamId;
    private long version;
    private String event;

    public StreamBufferEvent(final UUID streamId, final long version, final String event) {
        this.streamId = streamId;
        this.version = version;
        this.event = event;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getVersion() {
        return version;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("streamId", streamId)
                .append("version", version)
                .append("event", event)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        StreamBufferEvent that = (StreamBufferEvent) o;

        return new EqualsBuilder()
                .append(version, that.version)
                .append(streamId, that.streamId)
                .append(event, that.event)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(version)
                .append(event)
                .toHashCode();
    }

    @Override
    public int compareTo(StreamBufferEvent streamBufferEvent) {
        return toIntExact(this.getVersion() - streamBufferEvent.getVersion());
    }
}
