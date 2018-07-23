package uk.gov.justice.services.event.publisher.it.helpers;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_BROKER_URL;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_PASSWORD;
import static uk.gov.justice.services.event.publisher.it.helpers.JmsParameters.JMS_USERNAME;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class JmsMessageSender {


    private static final String PUBLISH_QUEUE_NAME = "publish.handler.command";

    private final ActiveMQConnectionFactory jmsConnectionFactory = new ActiveMQConnectionFactory(JMS_BROKER_URL);

    private Session session;
    private Connection connection;

    public void startSession() {

        try {
            connection = jmsConnectionFactory.createConnection(JMS_USERNAME, JMS_PASSWORD);
            connection.start();
            session = connection.createSession(false, AUTO_ACKNOWLEDGE);
        } catch (final JMSException e) {
            throw new JmsMessagingException("Failed to create JMS session");
        }
    }


    public void sendCommandToQueue(final JsonEnvelope jsonEnvelope, final String commandName) {

        try {
            final Destination destination = session.createQueue(PUBLISH_QUEUE_NAME);
            final MessageProducer messageProducer = session.createProducer(destination);

            @SuppressWarnings("deprecation") final String json = jsonEnvelope.toDebugStringPrettyPrint();

            final TextMessage message = session.createTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", commandName);

            messageProducer.send(message);
        } catch (final JMSException e) {
            throw new JmsMessagingException("Failed to send envelope", e);
        }
    }

    public void close() throws JMSException {

        if (session != null) {
            session.close();
        }

        if (connection != null) {
            connection.close();
        }
    }
}
