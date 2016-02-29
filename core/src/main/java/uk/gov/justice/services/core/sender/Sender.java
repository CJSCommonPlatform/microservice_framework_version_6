package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.messaging.Envelope;

/**
 * Sends an action to the next layer.
 */
public interface Sender {

    /**
     * Sends envelope to the next component.  The correct sender is injected by the framework.
     *
     * @param envelope Envelope that needs to be sent.
     */
    void send(final Envelope envelope);
}
