package uk.gov.justice.services.core.handler.registry.exception;

public class InvalidHandlerException extends RuntimeException {

    private static final long serialVersionUID = 3414630087471650179L;

    public InvalidHandlerException(final String message) {
        super(message);
    }
}
