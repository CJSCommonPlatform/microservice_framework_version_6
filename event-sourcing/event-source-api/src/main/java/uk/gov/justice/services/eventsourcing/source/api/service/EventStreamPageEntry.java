package uk.gov.justice.services.eventsourcing.source.api.service;

public class EventStreamPageEntry {
    private final String self;
    private final long sequenceNumber;

    public EventStreamPageEntry(final String self,
                                final long sequenceNumber) {
        this.self = self;
        this.sequenceNumber = sequenceNumber;
    }

    public String getSelf() {
        return self;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
}
