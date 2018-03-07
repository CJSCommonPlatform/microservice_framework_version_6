package uk.gov.justice.services.eventsourcing.source.api.service.core;

import java.util.UUID;

import javax.json.JsonObject;

public class EventEntry {
    private final String eventId;
    private final String streamId;
    private final String name;
    private final long position;
    private final String createdAt;
    private final JsonObject payload;


    public EventEntry(
            final UUID eventId,
            final UUID streamId,
            final long position,
            final String name,
            final JsonObject payload,
            final String createdAt
    ) {
        this.eventId = eventId.toString();
        this.streamId = streamId.toString();
        this.name = name;
        this.createdAt = createdAt;
        this.position = position;
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

    public long getPosition() {
        return position;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public JsonObject getPayload() {
        return payload;
    }


}
