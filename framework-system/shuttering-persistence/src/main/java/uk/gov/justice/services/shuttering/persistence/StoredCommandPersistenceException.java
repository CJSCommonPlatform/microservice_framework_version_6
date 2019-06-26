package uk.gov.justice.services.shuttering.persistence;

public class StoredCommandPersistenceException extends RuntimeException {

    public StoredCommandPersistenceException(final String message) {
        super(message);
    }

    public StoredCommandPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
