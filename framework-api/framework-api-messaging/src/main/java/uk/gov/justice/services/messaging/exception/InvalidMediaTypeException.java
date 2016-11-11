package uk.gov.justice.services.messaging.exception;

public class InvalidMediaTypeException extends RuntimeException {

    private static final long serialVersionUID = 7337362698441818463L;

    public InvalidMediaTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
