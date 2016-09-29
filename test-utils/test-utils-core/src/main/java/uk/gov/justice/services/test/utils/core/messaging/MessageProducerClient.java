package uk.gov.justice.services.test.utils.core.messaging;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.messaging.QueueUriProvider.queueUri;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.json.JsonObject;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

/**
 * Test utility class for sending messages to queues
 */
@SuppressWarnings("unused")
public class MessageProducerClient implements AutoCloseable {

    private static final String QUEUE_URI = queueUri();

    private Session session;
    private MessageProducer messageProducer;
    private Connection connection;

    /**
     * Starts the message producer for a specific topic. Must be called
     * before any messages can be sent.
     *
     * @param topicName the name of the topic to send to
     */
    public void startProducer(final String topicName) {

        try {
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_URI);
            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            final Destination destination = session.createTopic(topicName);
            messageProducer = session.createProducer(destination);
        } catch (JMSException e) {
            close();
            throw new RuntimeException("Failed to create message producer to topic: '" + topicName + "', queue uri: '" + QUEUE_URI + "'", e);
        }
    }

    /**
     * Sends a message to the topic specified in <code>startProducer(...)</code>
     *
     * @param commandName the name of the command
     * @param payload the payload to be wrapped in a simple JsonEnvelope
     */
    public void sendMessage(final String commandName, final JsonObject payload) {

        final JsonEnvelope jsonEnvelope = createEnvelope(commandName, payload);

        sendMessage(commandName, jsonEnvelope);
    }

    /**
     * Sends a message to the topic specified in <code>startProducer(...)</code>
     *
     * @param commandName the name of the command
     * @param jsonEnvelope the full JsonEnvelope to send as a message
     */
    public void sendMessage(final String commandName, final JsonEnvelope jsonEnvelope) {
        if (messageProducer == null) {
            close();
            throw new RuntimeException("Message producer not started. Please call startProducer(...) first.");
        }

        @SuppressWarnings("deprecation")
        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        try {
            final TextMessage message = session.createTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", commandName);

            messageProducer.send(message);
        } catch (JMSException e) {
            close();
            throw new RuntimeException("Failed to send message. commandName: '" + commandName + "', json: " + json, e);
        }
    }

    /**
     * closes all open resources
     */
    @Override
    public void close() {
        close(messageProducer);
        close(session);
        close(connection);

        session = null;
        messageProducer = null;
        connection = null;
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
