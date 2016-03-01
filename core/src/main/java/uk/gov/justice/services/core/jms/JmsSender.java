package uk.gov.justice.services.core.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;
import uk.gov.justice.services.core.util.JsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@ApplicationScoped
public class JmsSender {

    static final String JMS_HEADER_CPPNAME = "CPPNAME";

    Logger logger = LoggerFactory.getLogger(JmsSender.class);

    @Inject
    JsonObjectConverter jsonObjectConverter;

    @Resource(mappedName = "java:comp/DefaultJMSConnectionFactory")
    QueueConnectionFactory queueConnectionFactory;

    Context initialContext;

    private Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }


    /**
     * Sends the <code>envelope</code> to the JMS queue <code>queueName</code>
     *
     * @param queueName Name of the queue.
     * @param envelope  Envelope that needs to be sent.
     */
    public void send(final String queueName, final Envelope envelope) {

        try {
            final Queue queue = (Queue) getInitialContext().lookup(queueName);

            try (QueueConnection queueConnection = queueConnectionFactory.createQueueConnection()) {

                try (QueueSession session = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE)) {

                    final String envelopeAsString = jsonObjectConverter.asString(jsonObjectConverter.fromEnvelope(envelope));
                    final TextMessage textMessage = session.createTextMessage(envelopeAsString);
                    textMessage.setStringProperty(JMS_HEADER_CPPNAME, envelope.metadata().name());

                    try (QueueSender sender = session.createSender(queue)) {
                        sender.send(textMessage);
                    }
                }
            }

        } catch (JMSException | NamingException e) {
            throw new JmsSenderException("Exception while sending command to the controller.", e);
        }
    }

}
