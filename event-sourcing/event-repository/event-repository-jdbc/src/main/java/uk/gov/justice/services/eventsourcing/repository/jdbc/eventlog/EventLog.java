package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class to represent a persisted event.
 */
public class EventLog {

    private final UUID id;
    private final UUID streamId;
    private final Long sequenceId;
    private final String name;
    private final String payload;
    private final String metadata;
    private final ZonedDateTime dateCreated;

    public EventLog(final UUID id, final UUID streamId, final Long sequenceId, final String name, final String metadata, final String payload, final ZonedDateTime timestamp) {
        this.id = id;
        this.streamId = streamId;
        this.sequenceId = sequenceId;
        this.name = name;
        this.metadata = metadata;
        this.payload = payload;
        this.dateCreated = timestamp;
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

    public ZonedDateTime getDateCreated() {
        return dateCreated;
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
        final EventLog eventLog = (EventLog) o;
        return Objects.equals(id, eventLog.id) &&
                Objects.equals(streamId, eventLog.streamId) &&
                Objects.equals(sequenceId, eventLog.sequenceId) &&
                Objects.equals(payload, eventLog.payload) &&
                Objects.equals(metadata, eventLog.metadata) &&
                Objects.equals(name, eventLog.name) &&
                Objects.equals(dateCreated, eventLog.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, streamId, sequenceId, payload, name, metadata, dateCreated);
    }

    @Override
    public String toString() {
        return String.format("EventLog [id=%s, streamId=%s, sequenceId=%s, name=%s, payload=%s, metadata=%s, dateCreated=$s]", id,
                streamId, sequenceId, name, payload, metadata, ZonedDateTimes.toString(dateCreated));
    }

}