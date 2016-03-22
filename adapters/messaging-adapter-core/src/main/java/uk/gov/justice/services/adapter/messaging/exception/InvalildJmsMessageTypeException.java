package uk.gov.justice.services.adapter.messaging.exception;

public class InvalildJmsMessageTypeException extends RuntimeException {

    private static final long serialVersionUID = -3614445244093906063L;

    public InvalildJmsMessageTypeException(final String message) {
        super(message);
    }

    public InvalildJmsMessageTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
