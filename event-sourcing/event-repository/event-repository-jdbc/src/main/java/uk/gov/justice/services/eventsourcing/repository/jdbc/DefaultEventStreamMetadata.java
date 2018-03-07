package uk.gov.justice.services.eventsourcing.repository.jdbc;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 *  Implementation of {@link EventStreamMetadata}
 */
public class DefaultEventStreamMetadata implements EventStreamMetadata {

    private final UUID streamId;
    private final long position;
    private final boolean active;
    private final ZonedDateTime createdAt;

    public DefaultEventStreamMetadata(final UUID streamId,
                       final long position,
                       final boolean active,
                       final ZonedDateTime createdAt) {
        this.streamId = streamId;
        this.position = position;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public long getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
