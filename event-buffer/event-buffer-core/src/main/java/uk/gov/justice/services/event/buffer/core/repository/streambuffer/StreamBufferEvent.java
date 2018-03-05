package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.Math.toIntExact;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class StreamBufferEvent implements Comparable<StreamBufferEvent> {
    private final UUID streamId;
    private final long version;
    private final String event;
    private final String source;

    public StreamBufferEvent(final UUID streamId, final long version, final String event,
                             final String source) {
        this.streamId = streamId;
        this.version = version;
        this.event = event;
        this.source = source;
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

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "StreamBufferEvent{" +
                "streamId=" + streamId +
                ", version=" + version +
                ", event='" + event + '\'' +
                ", source='" + source + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StreamBufferEvent that = (StreamBufferEvent) o;

        return new EqualsBuilder()
                .append(getVersion(), that.getVersion())
                .append(getStreamId(), that.getStreamId())
                .append(getEvent(), that.getEvent())
                .append(getSource(), that.getSource())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStreamId())
                .append(getVersion())
                .append(getEvent())
                .append(getSource())
                .toHashCode();
    }

    @Override
    public int compareTo(final StreamBufferEvent streamBufferEvent) {
        return toIntExact(this.getVersion() - streamBufferEvent.getVersion());
    }
}
