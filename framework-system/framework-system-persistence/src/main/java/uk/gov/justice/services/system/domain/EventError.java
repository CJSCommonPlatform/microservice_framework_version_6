package uk.gov.justice.services.system.domain;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class EventError {

    private final UUID eventId;
    private final Long eventNumber;
    private final String component;
    private final String messageId;
    private final String metadata;
    private final String payload;
    private final String errorMessage;
    private final String stacktrace;
    private final ZonedDateTime erroredAt;

    public EventError(
            final UUID eventId,
            final Long eventNumber,
            final String component,
            final String messageId,
            final String metadata,
            final String payload,
            final String errorMessage,
            final String stacktrace,
            final ZonedDateTime erroredAt) {
        this.eventId = eventId;
        this.eventNumber = eventNumber;
        this.component = component;
        this.messageId = messageId;
        this.metadata = metadata;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
        this.erroredAt = erroredAt;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Long getEventNumber() {
        return eventNumber;
    }

    public String getComponent() {
        return component;
    }

    public String getMessageId() {
        return messageId;
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
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(eventNumber, that.eventNumber) &&
                Objects.equals(component, that.component) &&
                Objects.equals(messageId, that.messageId) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(stacktrace, that.stacktrace) &&
                Objects.equals(erroredAt, that.erroredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, eventNumber, component, messageId, metadata, payload, errorMessage, stacktrace, erroredAt);
    }

    @Override
    public String toString() {
        return "EventError{" +
                "eventId=" + eventId +
                ", eventNumber=" + eventNumber +
                ", component='" + component + '\'' +
                ", messageId='" + messageId + '\'' +
                ", metadata='" + metadata + '\'' +
                ", payload='" + payload + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", stacktrace='" + stacktrace + '\'' +
                ", erroredAt=" + erroredAt +
                '}';
    }
}
