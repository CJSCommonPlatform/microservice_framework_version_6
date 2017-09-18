package uk.gov.justice.services.eventsourcing.source.api.feed.eventstream;

import java.util.UUID;

public class EventStreamEntry {

    private final long sequenceId;
    private final String streamId;
    private final String selfHref;

    public EventStreamEntry(long sequenceId, final UUID streamId, final String selfHref) {
        this.sequenceId = sequenceId;
        this.streamId = streamId.toString();
        this.selfHref = selfHref;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getSelfHref() {
        return selfHref;
    }

    public long getSequenceId() {
        return sequenceId;
    }

}
