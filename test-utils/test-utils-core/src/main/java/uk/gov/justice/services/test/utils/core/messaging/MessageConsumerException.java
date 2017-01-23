package uk.gov.justice.services.test.utils.core.messaging;

public class MessageConsumerException extends RuntimeException {

    public MessageConsumerException(final String message) {
        super(message);
    }

    public MessageConsumerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
