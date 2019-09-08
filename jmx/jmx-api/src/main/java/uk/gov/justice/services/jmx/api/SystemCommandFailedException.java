package uk.gov.justice.services.jmx.api;

public class SystemCommandFailedException extends RuntimeException {

    private final String serverStackTrace;

    public SystemCommandFailedException(final String message, final String serverStackTrace) {
        super(message);
        this.serverStackTrace = serverStackTrace;
    }

    public String getServerStackTrace() {
        return serverStackTrace;
    }
}
