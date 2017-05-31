package uk.gov.justice.services.test.utils.core.messaging;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

public class MessageConsumerFactory {

    private Session session;
    private MessageConsumer messageConsumer;
    private JmsSessionFactory jmsSessionFactory;



    public MessageConsumerFactory() {
        jmsSessionFactory = new JmsSessionFactory();
    }

    public MessageConsumer createAndStart(
            final String messageSelector,
            final String queueUri,
            final String topicName) throws JMSException {

        session = jmsSessionFactory.session(queueUri);
        final ActiveMQTopic topic = new ActiveMQTopic(topicName);
        messageConsumer = session.createConsumer(
                topic,
                messageSelector);

        return messageConsumer;
    }

    public void close() {
        doClose(messageConsumer);
        jmsSessionFactory.close();
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
