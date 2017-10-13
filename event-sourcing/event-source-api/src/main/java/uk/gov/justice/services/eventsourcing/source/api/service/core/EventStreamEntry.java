package uk.gov.justice.services.eventsourcing.source.api.service.core;

public class EventStreamEntry {
    private final String streamId;
    private final long sequenceNumber;

    public EventStreamEntry(final String streamId,
                            final long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
}
