package uk.gov.justice.services.eventsourcing.source.core.exception;

/**
 * Exception raised when an event stream cannot be stored.
 */
public class EventStreamException extends Exception {

    public EventStreamException(final String message) {
        super(message);
    }

    public EventStreamException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
