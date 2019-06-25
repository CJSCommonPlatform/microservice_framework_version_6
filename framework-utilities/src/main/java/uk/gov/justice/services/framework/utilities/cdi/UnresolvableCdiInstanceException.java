package uk.gov.justice.services.framework.utilities.cdi;

public class UnresolvableCdiInstanceException extends RuntimeException {

    public UnresolvableCdiInstanceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
