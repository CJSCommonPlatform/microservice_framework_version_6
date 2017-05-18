package uk.gov.justice.services.test.utils.core.messaging;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class JmsSessionFactory {

    private Session session;
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;

    public Session session(String queueUri) {
        connectionFactory = new ActiveMQConnectionFactory(queueUri);

        try {
            connection = connectionFactory.createQueueConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return session;
        } catch (JMSException e) {
            throw new MessageConsumerException("Unable to create JMS session", e);
        }
    }

    public void close() {
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
