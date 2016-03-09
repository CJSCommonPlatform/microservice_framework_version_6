package uk.gov.justice.services.messaging.exception;

public class InvalidNameException extends RuntimeException {

    private static final long serialVersionUID = 2875567008075491148L;

    public InvalidNameException(final String message) {
        super(message);
    }

}
