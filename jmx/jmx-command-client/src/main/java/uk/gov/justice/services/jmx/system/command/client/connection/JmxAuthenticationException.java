package uk.gov.justice.services.jmx.system.command.client.connection;

public class JmxAuthenticationException extends RuntimeException {

    public JmxAuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
