package uk.gov.justice.services.jdbc.persistence;

public class DataAccessException extends RuntimeException {

    public DataAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
