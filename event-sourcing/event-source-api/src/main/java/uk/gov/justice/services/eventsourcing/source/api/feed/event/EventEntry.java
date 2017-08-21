package uk.gov.justice.services.eventsourcing.source.api.feed.event;

import java.time.ZonedDateTime;
import java.util.UUID;

public class EventEntry {
    private final String eventId;
    private final String streamId;
    private final String name;
    private final long sequenceId;
    private final ZonedDateTime createdAt;
    private final EventPayload payload;


    public EventEntry(final UUID eventId,
                      final UUID streamId,
                      final String name,
                      final long sequenceId,
                      final ZonedDateTime createdAt,
                      final EventPayload payload) {
        this.eventId = eventId.toString();
        this.streamId = streamId.toString();
        this.name = name;
        this.createdAt = createdAt;
        this.sequenceId = sequenceId;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getName() {
        return name;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public EventPayload getPayload() {
        return payload;
    }


}
