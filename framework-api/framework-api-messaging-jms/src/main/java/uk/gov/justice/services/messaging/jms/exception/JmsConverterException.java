package uk.gov.justice.services.messaging.jms.exception;

public class JmsConverterException extends RuntimeException {

    private static final long serialVersionUID = -1385799003531742131L;

    public JmsConverterException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
