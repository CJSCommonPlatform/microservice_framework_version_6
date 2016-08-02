package uk.gov.justice.services.event.stream.status.repository.jdbc;

import java.util.UUID;

/**
 * Entity to represent event stream status
 */
public class StreamStatus {

    private UUID streamId;
    private Long version;

    public StreamStatus(final UUID streamId, final Long version) {
        this.streamId = streamId;
        this.version = version;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getVersion() {
        return version;
    }
}
