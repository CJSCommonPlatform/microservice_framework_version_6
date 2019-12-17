package uk.gov.justice.services.system.domain;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EventError {

    private final String messageId;
    private final String component;
    private final String eventName;
    private final UUID eventId;
    private final Optional<UUID> streamId;
    private final Optional<Long> eventNumber;
    private final String metadata;
    private final String payload;
    private final String errorMessage;
    private final String stacktrace;
    private final ZonedDateTime erroredAt;

    public EventError(
            final String messageId,
            final String component,
            final String eventName,
            final UUID eventId,
            final Optional<UUID> streamId,
            final Optional<Long> eventNumber,
            final String metadata,
            final String payload,
            final String errorMessage,
            final String stacktrace,
            final ZonedDateTime erroredAt) {
        this.messageId = messageId;
        this.component = component;
        this.eventName = eventName;
        this.eventId = eventId;
        this.streamId = streamId;
        this.eventNumber = eventNumber;
        this.metadata = metadata;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
        this.erroredAt = erroredAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getComponent() {
        return component;
    }

    public String getEventName() {
        return eventName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Optional<UUID> getStreamId() {
        return streamId;
    }

    public Optional<Long> getEventNumber() {
        return eventNumber;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getPayload() {
        return payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public ZonedDateTime getErroredAt() {
        return erroredAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventError)) return false;
        final EventError that = (EventError) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(component, that.component) &&
                Objects.equals(eventName, that.eventName) &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(streamId, that.streamId) &&
                Objects.equals(eventNumber, that.eventNumber) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(stacktrace, that.stacktrace) &&
                Objects.equals(erroredAt, that.erroredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, component, eventName, eventId, streamId, eventNumber, metadata, payload, errorMessage, stacktrace, erroredAt);
    }

    @Override
    public String toString() {
        return "EventError{" +
                "messageId='" + messageId + '\'' +
                ", component='" + component + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventId=" + eventId +
                ", streamId=" + streamId +
                ", eventNumber=" + eventNumber +
                ", metadata='" + metadata + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", erroredAt=" + erroredAt +
                '}';
    }
}
