package uk.gov.justice.services.jmx.api;

public class SystemCommandInvocationFailedException extends RuntimeException {

    private final String serverStackTrace;

    public SystemCommandInvocationFailedException(final String message, final String serverStackTrace) {
        super(message);
        this.serverStackTrace = serverStackTrace;
    }

    public String getServerStackTrace() {
        return serverStackTrace;
    }
}
