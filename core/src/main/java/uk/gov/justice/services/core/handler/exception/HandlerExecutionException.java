package uk.gov.justice.services.core.handler.exception;

public class HandlerExecutionException extends RuntimeException {

    private static final long serialVersionUID = -4765722727204371744L;

    public HandlerExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
