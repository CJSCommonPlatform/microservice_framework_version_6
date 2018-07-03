package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.lang.Math.toIntExact;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class EventBufferEvent implements Comparable<EventBufferEvent> {
    private final UUID streamId;
    private final long position;
    private final String event;
    private final String source;

    public EventBufferEvent(final UUID streamId, final long position, final String event,
                            final String source) {
        this.streamId = streamId;
        this.position = position;
        this.event = event;
        this.source = source;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getPosition() {
        return position;
    }

    public String getEvent() {
        return event;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "EventBufferEvent{" +
                "streamId=" + streamId +
                ", position=" + position +
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

        final EventBufferEvent that = (EventBufferEvent) o;

        return new EqualsBuilder()
                .append(getPosition(), that.getPosition())
                .append(getStreamId(), that.getStreamId())
                .append(getEvent(), that.getEvent())
                .append(getSource(), that.getSource())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStreamId())
                .append(getPosition())
                .append(getEvent())
                .append(getSource())
                .toHashCode();
    }

    @Override
    public int compareTo(final EventBufferEvent eventBufferEvent) {
        return toIntExact(this.getPosition() - eventBufferEvent.getPosition());
    }
}
