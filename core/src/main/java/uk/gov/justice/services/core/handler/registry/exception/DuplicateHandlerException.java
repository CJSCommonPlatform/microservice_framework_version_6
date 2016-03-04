package uk.gov.justice.services.core.handler.registry.exception;

public class DuplicateHandlerException extends RuntimeException {

    private static final long serialVersionUID = -411625483514506602L;

    public DuplicateHandlerException(final String message) {
        super(message);
    }
}
