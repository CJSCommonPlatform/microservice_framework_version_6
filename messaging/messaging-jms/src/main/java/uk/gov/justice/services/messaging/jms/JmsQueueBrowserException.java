package uk.gov.justice.services.messaging.jms;

public class JmsQueueBrowserException extends RuntimeException {

    public JmsQueueBrowserException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
