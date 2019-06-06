package uk.gov.justice.services.messaging.jms;

public class JmsQueueBrowserUnsupportedOperation extends RuntimeException {

    public JmsQueueBrowserUnsupportedOperation(final String message) {
        super(message);
    }
}
