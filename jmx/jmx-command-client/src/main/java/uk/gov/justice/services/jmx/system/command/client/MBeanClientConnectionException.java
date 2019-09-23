package uk.gov.justice.services.jmx.system.command.client;

public class MBeanClientConnectionException extends RuntimeException {

    public MBeanClientConnectionException(final String message) {
        super(message);
    }

    public MBeanClientConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
