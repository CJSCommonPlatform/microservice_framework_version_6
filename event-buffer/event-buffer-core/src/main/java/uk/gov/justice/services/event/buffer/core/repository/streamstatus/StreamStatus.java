package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Entity to represent event stream status
 */
public class StreamStatus {

    private final UUID streamId;
    private final long version;
    private final String source;

    public StreamStatus(final UUID streamId, final long version, final String source) {
        this.streamId = streamId;
        this.version = version;
        this.source = source;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getVersion() {
        return version;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "StreamStatus{" +
                "streamId=" + streamId +
                ", version=" + version +
                ", source='" + source + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        StreamStatus that = (StreamStatus) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(version, that.version)
                .append(source, that.source)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(version)
                .append(source)
                .toHashCode();
    }
}
