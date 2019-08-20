package uk.gov.justice.services.jmx.api;

public class SystemCommandException extends RuntimeException {

    public SystemCommandException(final String message) {
        super(message);
    }

    public SystemCommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
