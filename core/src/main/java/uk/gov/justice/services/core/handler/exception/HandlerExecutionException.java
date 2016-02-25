package uk.gov.justice.services.core.handler.exception;

public class HandlerExecutionException extends RuntimeException {

    public HandlerExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
