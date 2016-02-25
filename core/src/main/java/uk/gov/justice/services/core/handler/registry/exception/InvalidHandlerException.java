package uk.gov.justice.services.core.handler.registry.exception;

public class InvalidHandlerException extends RuntimeException {
    public InvalidHandlerException(final String message) {
        super(message);
    }
}
