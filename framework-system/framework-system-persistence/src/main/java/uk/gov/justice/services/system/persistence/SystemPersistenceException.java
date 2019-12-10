package uk.gov.justice.services.system.persistence;

public class SystemPersistenceException extends RuntimeException {

    public SystemPersistenceException(final String message) {
        super(message);
    }

    public SystemPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
