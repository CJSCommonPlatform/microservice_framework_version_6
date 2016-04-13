package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Sends an action to the next layer.
 */
@FunctionalInterface
public interface Sender {

    /**
     * Sends envelope to the next component.  The correct sender is injected by the framework.
     *
     * @param envelope JsonEnvelope that needs to be sent.
     */
    void send(final JsonEnvelope envelope);
}
