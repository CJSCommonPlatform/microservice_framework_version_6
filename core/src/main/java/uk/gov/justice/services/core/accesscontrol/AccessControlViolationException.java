package uk.gov.justice.services.core.accesscontrol;

public class AccessControlViolationException extends RuntimeException {

    public AccessControlViolationException(final String message) {
        super(message);
    }
}
