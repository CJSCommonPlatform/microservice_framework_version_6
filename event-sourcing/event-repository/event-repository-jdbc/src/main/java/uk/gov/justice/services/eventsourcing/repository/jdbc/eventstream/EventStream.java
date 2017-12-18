package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import java.util.UUID;

public class EventStream {

    private final UUID streamId;
    private Long sequenceNumber;
    private boolean active;

    public EventStream(final UUID streamId) {
        this.streamId = streamId;
    }

    public EventStream(final UUID streamId, final Long sequenceNumber, final boolean active) {
        this.streamId = streamId;
        this.sequenceNumber = sequenceNumber;
        this.active = active;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isActive() {
        return active;
    }
}
