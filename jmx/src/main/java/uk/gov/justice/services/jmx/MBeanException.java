package uk.gov.justice.services.jmx;

public class MBeanException extends RuntimeException {
    public MBeanException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
