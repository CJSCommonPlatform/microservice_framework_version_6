package uk.gov.justice.services.core.jms.exception;

public class JmsSenderException extends RuntimeException {

    private static final long serialVersionUID = 6632104471706025279L;

    public JmsSenderException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
