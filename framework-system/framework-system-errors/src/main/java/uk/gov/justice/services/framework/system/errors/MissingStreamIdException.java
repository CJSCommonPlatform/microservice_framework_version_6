package uk.gov.justice.services.framework.system.errors;

public class MissingStreamIdException extends RuntimeException {

    public MissingStreamIdException(final String message) {
        super(message);
    }
}
