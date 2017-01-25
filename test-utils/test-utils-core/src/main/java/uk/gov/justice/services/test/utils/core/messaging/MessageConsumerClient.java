package uk.gov.justice.services.test.utils.core.messaging;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import com.google.common.annotations.VisibleForTesting;

public class MessageConsumerClient implements AutoCloseable {

    public static final long TIMEOUT_IN_MILLIS = 20_000;
    public static final String QUEUE_URI = queueUri();

    private static final String MESSAGE_SELECTOR_TEMPLATE = "CPPNAME IN ('%s')";


    private final MessageConsumerFactory messageConsumerFactory;

    private MessageConsumer messageConsumer;

    public MessageConsumerClient() {
        this(new MessageConsumerFactory());
    }

    @VisibleForTesting
    MessageConsumerClient(final MessageConsumerFactory messageConsumerFactory) {
        this.messageConsumerFactory = messageConsumerFactory;
    }

    public void startConsumer(final String eventName, final String topicName) {

        final String messageSelector = format(MESSAGE_SELECTOR_TEMPLATE, eventName);

        try {
            messageConsumer = messageConsumerFactory.createAndStart(
                    messageSelector,
                    QUEUE_URI,
                    topicName);

        } catch (final JMSException e) {
            close();
            final String message = "Failed to start message consumer for " +
                    "eventName '" + eventName + "':, " +
                    "topic: '" + topicName + "', " +
                    "queueUri: '" + QUEUE_URI + "', " +
                    "messageSelector: '" + messageSelector + "'";
            throw new MessageConsumerException(message, e);
        }
    }

    public Optional<String> retrieveMessageNoWait() {
        return retrieve(() -> messageConsumer.receiveNoWait());
    }

    public Optional<String> retrieveMessage() {
        return retrieveMessage(TIMEOUT_IN_MILLIS);
    }

    public Optional<String> retrieveMessage(final long timeout) {
        return retrieve(() -> messageConsumer.receive(timeout));
    }

    private Optional<String> retrieve(final MessageSupplier messageSupplier) {

        if (messageConsumer == null) {
            throw new MessageConsumerException("Message consumer not started. Please call startConsumer(...) first.");
        }

        try {
            final TextMessage message = (TextMessage) messageSupplier.getMessage();
            if (message == null) {
                return empty();
            }
            return of(message.getText());
        } catch (final JMSException e) {
            throw new MessageConsumerException("Failed to retrieve message", e);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void cleanQueue() {
        while (retrieveMessageNoWait().isPresent()) {
        }
    }

    @Override
    public void close() {
        messageConsumerFactory.close();
    }


    @VisibleForTesting
    MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    @FunctionalInterface
    private interface MessageSupplier {
        Message getMessage() throws JMSException;
    }
}
