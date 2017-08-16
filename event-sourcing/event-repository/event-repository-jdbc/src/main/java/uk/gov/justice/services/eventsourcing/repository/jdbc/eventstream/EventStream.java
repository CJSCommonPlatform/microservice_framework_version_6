package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import java.util.UUID;

public class EventStream {

    private final UUID streamId;
    private Long sequenceNumber;

    public EventStream(final UUID streamId) {
        this.streamId = streamId;
    }

    public EventStream(final UUID streamId, final Long sequenceNumber) {
        this.streamId = streamId;
        this.sequenceNumber = sequenceNumber;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }
}
