package uk.gov.justice.services.core.handler.exception;

public class MissingHandlerException extends RuntimeException {

    public MissingHandlerException(final String message) {
        super(message);
    }

}
