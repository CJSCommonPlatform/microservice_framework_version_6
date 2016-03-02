package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.context.ContextName;
import uk.gov.justice.services.core.jms.converter.EnvelopeConverter;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.inject.Alternative;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Objects;

@Alternative
public class JmsSender implements Sender {

    private final JmsEndpoints jmsEndpoints;
    private final Component destinationComponent;
    private final QueueConnectionFactory queueConnectionFactory;

    Context initialContext;
    private EnvelopeConverter envelopeConverter;

    public JmsSender(final Component destinationComponent, final EnvelopeConverter envelopeConverter, final JmsEndpoints jmsEndpoints,
                     final QueueConnectionFactory queueConnectionFactory) {
        this.envelopeConverter = envelopeConverter;
        this.destinationComponent = destinationComponent;
        this.jmsEndpoints = jmsEndpoints;
        this.queueConnectionFactory = queueConnectionFactory;
    }

    private Context getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    @Override
    public void send(Envelope envelope) {
        final String contextName = ContextName.fromName(envelope.metadata().name());
        send(jmsEndpoints.getEndpoint(destinationComponent, contextName), envelope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JmsSender jmsSender = (JmsSender) o;
        return destinationComponent == jmsSender.destinationComponent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationComponent);
    }

    /**
     * Sends the <code>envelope</code> to the JMS queue <code>queueName</code>
     *
     * @param queueName Name of the queue.
     * @param envelope  Envelope that needs to be sent.
     */
    private void send(final String queueName, final Envelope envelope) {

        try {
            final Queue queue = (Queue) getInitialContext().lookup(queueName);

            try (QueueConnection queueConnection = queueConnectionFactory.createQueueConnection()) {

                try (QueueSession session = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE)) {

                    try (QueueSender sender = session.createSender(queue)) {
                        sender.send(envelopeConverter.toMessage(envelope, session));
                    }
                }
            }

        } catch (JMSException | NamingException e) {
            throw new JmsSenderException("Exception while sending command to the controller.", e);
        }
    }

}
