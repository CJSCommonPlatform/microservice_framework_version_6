package uk.gov.justice.services.shuttering.persistence;

public class ShutteringPersistenceException extends RuntimeException {

    public ShutteringPersistenceException(final String message) {
        super(message);
    }

    public ShutteringPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
