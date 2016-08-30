package uk.gov.justice.services.eventsourcing.common.exception;

/**
 * Exception thrown when a request tries to store an event with invalid streamId.
 */
public class InvalidStreamIdException extends RuntimeException {

    private static final long serialVersionUID = -5575552152956600924L;

    public InvalidStreamIdException(String message) {
        super(message);
    }
}
