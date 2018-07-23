package uk.gov.justice.services.event.publisher.it.helpers;

import static java.lang.String.format;
import static java.util.Collections.synchronizedList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_BROKER_URL;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_PASSWORD;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_USERNAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;

public class JmsMessageReceiver {


    private static final String PUBLISH_TOPIC_NAME = "publish.event";

    private final ActiveMQConnectionFactory jmsConnectionFactory = new ActiveMQConnectionFactory(JMS_BROKER_URL);

    private Session session;
    private Connection connection;

    private final AtomicBoolean messageConsumerStarted = new AtomicBoolean(false);

    final List<String> messageList = synchronizedList(new ArrayList<>());


    public void startTopicListener(final String eventName, final int expectedNumberOfMessages) {

        new Thread(() -> {

            final MessageConsumer messageConsumer;
            try {
                connection = jmsConnectionFactory.createConnection(JMS_USERNAME, JMS_PASSWORD);
                connection.start();
                session = connection.createSession(false, AUTO_ACKNOWLEDGE);
                final ActiveMQTopic destination = new ActiveMQTopic(PUBLISH_TOPIC_NAME);
                messageConsumer = session.createConsumer(
                        destination,
                        format("CPPNAME IN ('%s')", eventName));

                messageConsumerStarted.set(true);
            } catch (final JMSException e) {
                throw new JmsMessagingException("Failed to create topic message consumer", e);
            }

            while (messageList.size() <= expectedNumberOfMessages && messageConsumerStarted.get()) {
                final Message receivedMessage;

                try {
                    receivedMessage = messageConsumer.receiveNoWait();
                    if (receivedMessage != null) {
                        final String messageBody = receivedMessage.getBody(String.class);
                        messageList.add(messageBody);
                    }
                } catch (final JMSException e) {
                    throw new JmsMessagingException("Failed to receive message", e);
                }
            }

        }).start();
    }

    public Optional<List<String>> getMessagesFromTopic(final int expectedNumberOfMessages) {
        if (messageList.size() < expectedNumberOfMessages) {
            return empty();
        }

        return of(messageList);
    }

    public void close() throws JMSException {

        messageConsumerStarted.set(false);

        if (session != null) {
            session.close();
        }

        if (connection != null) {
            connection.close();
        }
    }
}
