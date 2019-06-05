package uk.gov.justice.services.jmx.command;

public class SystemCommandInvocationException extends RuntimeException {

    public SystemCommandInvocationException(final String message) {
        super(message);
    }

    public SystemCommandInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
