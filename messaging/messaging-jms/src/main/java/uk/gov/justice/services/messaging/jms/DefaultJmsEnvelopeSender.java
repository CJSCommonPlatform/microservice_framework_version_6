package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

/**
 * An envelope producer that sends or publishes an envelope to a queue or topic respectively
 * depending on the destination type.
 */
public class DefaultJmsEnvelopeSender implements JmsEnvelopeSender {

    @Inject
    private EnvelopeSenderSelector envelopeSenderSelector;

    /**
     * Sends envelope to the destination via JMS.
     *
     * @param envelope        envelope to be sent.
     * @param destinationName JNDI name of the JMS destination.
     */
    @Override
    public void send(final JsonEnvelope envelope, final String destinationName) {
        envelopeSenderSelector.getEnvelopeSender().send(envelope, destinationName);
    }
}
