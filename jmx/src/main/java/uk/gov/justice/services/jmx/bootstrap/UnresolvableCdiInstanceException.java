package uk.gov.justice.services.jmx.bootstrap;

public class UnresolvableCdiInstanceException extends RuntimeException {

    public UnresolvableCdiInstanceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
