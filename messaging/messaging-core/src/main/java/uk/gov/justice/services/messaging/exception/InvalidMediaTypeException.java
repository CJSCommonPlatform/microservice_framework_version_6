package uk.gov.justice.services.messaging.exception;

public class InvalidMediaTypeException extends RuntimeException {

    public InvalidMediaTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
