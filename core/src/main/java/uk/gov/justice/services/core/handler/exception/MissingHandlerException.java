package uk.gov.justice.services.core.handler.exception;

public class MissingHandlerException extends RuntimeException {

    private static final long serialVersionUID = -5052702793269107242L;

    public MissingHandlerException(final String message) {
        super(message);
    }

}
