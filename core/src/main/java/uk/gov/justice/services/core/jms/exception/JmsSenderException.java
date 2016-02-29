package uk.gov.justice.services.core.jms.exception;

public class JmsSenderException extends RuntimeException {

    public JmsSenderException(final String message, final Throwable cause) {
        super(message, cause);
    }

}