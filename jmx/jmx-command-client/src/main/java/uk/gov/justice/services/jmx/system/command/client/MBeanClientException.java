package uk.gov.justice.services.jmx.system.command.client;

public class MBeanClientException extends RuntimeException {

    public MBeanClientException(final String message) {
        super(message);
    }

    public MBeanClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
