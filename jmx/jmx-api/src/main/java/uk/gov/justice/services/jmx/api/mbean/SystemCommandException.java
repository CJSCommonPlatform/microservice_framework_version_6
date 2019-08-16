package uk.gov.justice.services.jmx.api.mbean;

public class SystemCommandException extends RuntimeException {

    public SystemCommandException(final String message) {
        super(message);
    }

    public SystemCommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
