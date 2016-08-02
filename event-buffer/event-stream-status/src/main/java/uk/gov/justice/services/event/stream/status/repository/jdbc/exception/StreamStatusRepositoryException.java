package uk.gov.justice.services.event.stream.status.repository.jdbc.exception;

/**
 * Exception thrown when a request tries to store an event for an aggregate root version that
 * already exists in the database.
 */
public class StreamStatusRepositoryException extends RuntimeException {

    private static final long serialVersionUID = 5934757852541630746L;

    public StreamStatusRepositoryException(String message) {
        super(message);
    }

    public StreamStatusRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
