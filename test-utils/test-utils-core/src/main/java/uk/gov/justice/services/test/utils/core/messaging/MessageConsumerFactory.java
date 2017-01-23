package uk.gov.justice.services.test.utils.core.messaging;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

public class MessageConsumerFactory {

    private Session session;
    private MessageConsumer messageConsumer;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;

    public MessageConsumer createAndStart(
            final String messageSelector,
            final String queueUri,
            final String topicName) throws JMSException {

        connectionFactory = new ActiveMQConnectionFactory(queueUri);
        connection = connectionFactory.createConnection();

        connection.start();

        session = connection.createSession(false, AUTO_ACKNOWLEDGE);
        final ActiveMQTopic topic = new ActiveMQTopic(topicName);
        messageConsumer = session.createConsumer(
                topic,
                messageSelector);

        return messageConsumer;
    }

    public void close() {
        doClose(messageConsumer);
        doClose(session);
        doClose(connection);
        doClose(connectionFactory);
    }

    private void doClose(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
            }
        }
    }
}
