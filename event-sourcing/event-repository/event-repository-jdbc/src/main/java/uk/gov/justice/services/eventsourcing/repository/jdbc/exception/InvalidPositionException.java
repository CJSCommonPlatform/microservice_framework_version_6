package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

/**
 * Exception thrown when a request tries to store an event for an aggregate root version that
 * already exists in the database.
 */
public class InvalidPositionException extends Exception {

    private static final long serialVersionUID = 5934757852541630746L;

    public InvalidPositionException(String message) {
        super(message);
    }

}
