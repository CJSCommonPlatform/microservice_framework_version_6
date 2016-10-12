package uk.gov.moj.cpp.notification.integration.util;

import static java.lang.String.format;
import java.util.List;
import java.util.function.BiConsumer;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * Send messages to a particular topic sequentially <br/>
 * or as a parallel stream with autocloseable feature
 * 
 * Example: <br/>
 * 
 * <code>
 *  try (TopicSender ts = TopicSender.of(URI, "public.event")) {
            ts.send("JMS Envelope");
    }
 * </code>
 *
 */
public class TopicSender implements AutoCloseable {

    private final String uri;

    private final String topic;

    private Connection connection;

    private TopicSender(String uri, String topic) {
        this.uri = uri;
        this.topic = topic;
    }

    public static final TopicSender of(String uri, String topic) {
        return new TopicSender(uri, topic);
    }


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
     * Even one of the msgs failing throws a RuntimeException
     * 
     * @param msgs
     * @throws JMSException
     */
    public void send(List<String> msgs) throws JMSException {
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

    public void parallelSend(List<String> msgs) throws JMSException {
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

    private void exec(BiConsumer<MessageProducer, Session> op) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(uri);
        this.connection = factory.createConnection();
        this.connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(session.createTopic(topic));
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        op.accept(producer, session);
        session.close();
    }


    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                throw new RuntimeException("Exception closing connection", e);
            }
        }
    }
}
