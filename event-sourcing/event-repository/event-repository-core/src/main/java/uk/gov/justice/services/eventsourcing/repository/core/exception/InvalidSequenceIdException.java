package uk.gov.justice.services.eventsourcing.repository.core.exception;

/**
 * Exception thrown when a request tries to store an event for an aggregate root version that
 * already exists in the database.
 */
public class InvalidSequenceIdException extends Exception {

    private static final long serialVersionUID = 5934757852541630746L;

    public InvalidSequenceIdException(String message) {
        super(message);
    }

}
