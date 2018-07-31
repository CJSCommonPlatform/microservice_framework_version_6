package uk.gov.justice.services.event.publisher.it.helpers;

public class JmsMessagingException extends RuntimeException {

    public JmsMessagingException(final String message) {
        super(message);
    }

    public JmsMessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
