package uk.gov.justice.services.test.utils.core.messaging;


import static java.lang.String.format;

import java.util.List;
import java.util.function.BiConsumer;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

/**
 * 
 * Send messages to a JMS topic sequentially <br>
 * or as a parallel stream
 * 
 * Example: <br/>
 * 
 * <code>
 *
 *  TopicSender.of(URI, "public.event").send("JMS Envelope");
 *   
 * </code>
 *
 */
public class TopicSender {

    /**
     * The URI of the JMS server
     */
    private final String uri;

    /**
     * The topic to send messages to
     */
    private final String topic;

    /**
     * Initialise the sender
     * 
     * @param uri of the JMS server
     * @param topic to send messages to
     */
    private TopicSender(final String uri, final String topic) {
        this.uri = uri;
        this.topic = topic;
    }

    /**
     * Create an instance of the sender
     * 
     * @param uri of the JMS server
     * @param topic to send messages to
     * @return TopicSender
     */
    public static final TopicSender of(final String uri, final String topic) {
        return new TopicSender(uri, topic);
    }


    /**
     * Send a single message to the configured topic
     * 
     * @param msg to send
     * @throws JMSException if unsuccessful
     */
    public void send(final String msg) throws JMSException {
        exec((producer, session) -> {
            try {
                producer.send(session.createTextMessage(msg));
            } catch (JMSException e) {
                throw new RuntimeException(format("Failed to send %s", msg), e);
            }
        });
    }

    /**
     * Send a stream of messages to the configured topic <br>
     * <br>
     * Even one of the msgs failing throws a RuntimeException
     * 
     * @param msgs to send
     * @throws JMSException if unsuccessful
     */
    public void send(final List<String> msgs) throws JMSException {
        exec((producer, session) -> {
            msgs.stream().forEach((m) -> {
                try {
                    producer.send(session.createTextMessage(m));
                } catch (JMSException e) {
                    throw new RuntimeException(format("Failed to send %s", m), e);
                }
            });
        });
    }

    /**
     * Send a stream of messages in parallel to the configured topic <br>
     * <br>
     * Even one of the msgs failing throws a RuntimeException
     * 
     * @param msgs to send
     * @throws JMSException if unsuccessful
     */
    public void parallelSend(final List<String> msgs) throws JMSException {
        exec((producer, session) -> {
            msgs.parallelStream().forEach((m) -> {
                try {
                    producer.send(session.createTextMessage(m));
                } catch (JMSException e) {
                    throw new RuntimeException(format("Failed to send %s", m), e);
                }
            });
        });
    }

    /**
     * Execute the passed in operation injecting a producer and session
     * 
     * @param op to execute
     * @throws JMSException if unsuccessful
     */
    private void exec(BiConsumer<MessageProducer, Session> op) throws JMSException {
        try (final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(uri);
                        final Connection connection = factory.createConnection();) {
            connection.start();
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final MessageProducer producer = session.createProducer(session.createTopic(topic));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            op.accept(producer, session);
            session.close();
        }
    }
}
