package uk.gov.justice.services.eventsourcing.source.api.feed.eventstream;

import java.util.UUID;

public class EventStreamEntry {
    private final String streamId;
    private final String href;

    public EventStreamEntry(final UUID streamId, final String href) {
        this.streamId = streamId.toString();
        this.href = href;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getHref() {
        return href;
    }
}
