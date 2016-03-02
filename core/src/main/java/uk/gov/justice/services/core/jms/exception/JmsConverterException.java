package uk.gov.justice.services.core.jms.exception;

public class JmsConverterException extends RuntimeException {

    public JmsConverterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
