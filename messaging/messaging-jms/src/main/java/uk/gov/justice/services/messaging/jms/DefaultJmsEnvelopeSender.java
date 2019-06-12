package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * An JmsEnvelopeSender that sends or publishes an envelope to a queue or topic respectively
 * depending on the destination type.
 */
public class DefaultJmsEnvelopeSender implements JmsEnvelopeSender {

    private final JmsSender jmsSender;

    public DefaultJmsEnvelopeSender(final JmsSender jmsSender) {
        this.jmsSender = jmsSender;
    }

    /**
     * Sends envelope to the destination via JMS.
     *
     * @param envelope        envelope to be sent.
     * @param destinationName JNDI name of the JMS destination.
     */
    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        jmsSender.send(envelope, destinationName);
    }
}
