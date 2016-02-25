package uk.gov.justice.services.core.handler.registry.exception;

public class DuplicateHandlerException extends RuntimeException {
    public DuplicateHandlerException(final String message) {
        super(message);
    }
}
