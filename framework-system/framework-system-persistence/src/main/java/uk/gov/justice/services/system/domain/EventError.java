package uk.gov.justice.services.system.domain;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class EventError {

    private final String messageId;
    private final String component;
    private final UUID eventId;
    private final String eventName;
    private final Long eventNumber;
    private final String metadata;
    private final String payload;
    private final String errorMessage;
    private final String stacktrace;
    private final ZonedDateTime erroredAt;
    private final String comments;

    public EventError(
            final String messageId,
            final String component,
            final UUID eventId,
            final String eventName,
            final Long eventNumber,
            final String metadata,
            final String payload,
            final String errorMessage,
            final String stacktrace,
            final ZonedDateTime erroredAt,
            final String comments) {
        this.messageId = messageId;
        this.component = component;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventNumber = eventNumber;
        this.metadata = metadata;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
        this.erroredAt = erroredAt;
        this.comments = comments;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getComponent() {
        return component;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public Long getEventNumber() {
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

    public String getComments() {
        return comments;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventError)) return false;
        final EventError that = (EventError) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(component, that.component) &&
                Objects.equals(eventId, that.eventId) &&
                Objects.equals(eventName, that.eventName) &&
                Objects.equals(eventNumber, that.eventNumber) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(erroredAt, that.erroredAt) &&
                Objects.equals(comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, component, eventId, eventName, eventNumber, metadata, payload, errorMessage, stacktrace, erroredAt, comments);
    }

    @Override
    public String toString() {
        return "EventError{" +
                "messageId='" + messageId + '\'' +
                ", component='" + component + '\'' +
                ", eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", eventNumber=" + eventNumber +
                ", metadata='" + metadata + '\'' +
                ", payload='" + payload + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", erroredAt=" + erroredAt +
                ", comments='" + comments + '\'' +
                '}';
    }
}
