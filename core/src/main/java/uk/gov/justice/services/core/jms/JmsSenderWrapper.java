package uk.gov.justice.services.core.jms;


import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Wrapper for JMS senders for to accommodate the new JMS client generator and provide backwards compatibility.
 * If the primary sender throws and exception due to a missing handler then it should fail back to the secondary handler
 */
public class JmsSenderWrapper implements Sender {

    private final Sender primarySender;
    private final Sender legacySender;

    /**
     * @param primarySender the primary sender
     * @param legacySender legacy sender that is to be used if the primary one is not available
     */
    public JmsSenderWrapper(final Sender primarySender, final Sender legacySender) {
        this.primarySender = primarySender;
        this.legacySender = legacySender;
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        try {
            primarySender.send(envelope);
        } catch (MissingHandlerException e) {
            legacySender.send(envelope);
        }
    }
}
