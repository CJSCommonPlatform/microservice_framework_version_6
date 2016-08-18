package uk.gov.justice.services.test.utils.core.messaging;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

public class MessageConsumerClient implements AutoCloseable {

    private static final String EVENT_SELECTOR_TEMPLATE = "CPPNAME IN ('%s')";
    private static final String QUEUE_URI = System.getProperty("queueUri", "tcp://localhost:61616");
    private static final long TIMEOUT_IN_MILLIS = 20_000;

    private Session session;
    private ActiveMQTopic topic;
    private MessageConsumer messageConsumer;

    public void startConsumer(final String eventSelector, final String topicName) {
        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_URI);
            final Connection connection = factory.createConnection();

            connection.start();

            session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            topic = new ActiveMQTopic(topicName);
            messageConsumer = session.createConsumer(topic, String.format(EVENT_SELECTOR_TEMPLATE, eventSelector));

        } catch (JMSException e) {
            close();
            throw new RuntimeException("Failed to start message consumer for events: '" + eventSelector + "', topic: '" + topicName + ", queue uri: '" + QUEUE_URI + "'", e);
        }
    }

    public Optional<String> retrieveMessage() {

        if (messageConsumer == null) {
            throw new RuntimeException("Message consumer not started. Please call startConsumer(...) first.");
        }

        try {
            final TextMessage message = (TextMessage) messageConsumer.receive(TIMEOUT_IN_MILLIS);
            if (message == null) {
                return empty();
            }
            return of(message.getText());
        } catch (JMSException e) {
            throw new RuntimeException("Failed to retrieve message", e);
        }
    }

    @Override
    public void close() {
        close(messageConsumer);
        close(session);

        session = null;
        topic = null;
        messageConsumer = null;
    }

    private void close(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
