package uk.gov.justice.services.jdbc.persistence;

/**
 * Exception thrown when there's error accessing database
 */
public class JdbcRepositoryException extends RuntimeException {

    private static final long serialVersionUID = 5934757852541630746L;

    public JdbcRepositoryException(String message) {
        super(message);
    }

    public JdbcRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
    public JdbcRepositoryException(final Throwable cause) {
        super(cause);
    }
}
