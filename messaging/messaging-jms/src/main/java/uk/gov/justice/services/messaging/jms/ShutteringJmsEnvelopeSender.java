package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class ShutteringJmsEnvelopeSender implements JmsEnvelopeSender {

    private final EnvelopeSenderSelector envelopeSenderSelector;

    public ShutteringJmsEnvelopeSender(final EnvelopeSenderSelector envelopeSenderSelector) {
        this.envelopeSenderSelector = envelopeSenderSelector;
    }

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
