package uk.gov.justice.services.common.exception;


public class ForbiddenRequestException extends RuntimeException {
    public ForbiddenRequestException(final String message) {
        super(message);
    }
}
