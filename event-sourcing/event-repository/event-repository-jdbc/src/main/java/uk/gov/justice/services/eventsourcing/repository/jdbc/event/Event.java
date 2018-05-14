package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class to represent a persisted event.
 */
public class Event {

    private final UUID id;
    private final UUID streamId;
    private final Long sequenceId;
    private final String name;
    private final String payload;
    private final String metadata;
    private final ZonedDateTime createdAt;


    public Event(final UUID id,
                 final UUID streamId,
                 final Long sequenceId,
                 final String name,
                 final String metadata,
                 final String payload,
                 final ZonedDateTime createdAt) {
        this.id = id;
        this.streamId = streamId;
        this.sequenceId = sequenceId;
        this.name = name;
        this.metadata = metadata;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public String getPayload() {
        return payload;
    }

    public String getName() {
        return name;
    }

    public String getMetadata() {
        return metadata;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }


    @Override
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Event event = (Event) o;
        return Objects.equals(id, event.id) &&
                Objects.equals(streamId, event.streamId) &&
                Objects.equals(sequenceId, event.sequenceId) &&
                Objects.equals(payload, event.payload) &&
                Objects.equals(metadata, event.metadata) &&
                Objects.equals(name, event.name) &&
                Objects.equals(createdAt, event.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, streamId, sequenceId, payload, name, metadata, createdAt);
    }

    @Override
    public String toString() {
        return String.format("Event [id=%s, streamId=%s, sequenceId=%s, name=%s, payload=%s, metadata=%s, createdAt=$s, source=$s]", id,
                streamId, sequenceId, name, payload, metadata, ZonedDateTimes.toString(createdAt));
    }

}