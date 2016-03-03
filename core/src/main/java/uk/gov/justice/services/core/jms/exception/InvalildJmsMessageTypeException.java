package uk.gov.justice.services.core.jms.exception;

public class InvalildJmsMessageTypeException extends RuntimeException {

    public InvalildJmsMessageTypeException(final String message) {
        super(message);
    }

    public InvalildJmsMessageTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
