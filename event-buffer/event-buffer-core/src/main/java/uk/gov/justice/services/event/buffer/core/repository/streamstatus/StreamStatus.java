package uk.gov.justice.services.event.buffer.core.repository.streamstatus;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Entity to represent event stream status
 */
public class StreamStatus {

    private UUID streamId;
    private long version;

    public StreamStatus(final UUID streamId, final long version) {
        this.streamId = streamId;
        this.version = version;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("streamId", streamId)
                .append("version", version)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        StreamStatus that = (StreamStatus) o;

        return new EqualsBuilder()
                .append(streamId, that.streamId)
                .append(version, that.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(streamId)
                .append(version)
                .toHashCode();
    }
}
