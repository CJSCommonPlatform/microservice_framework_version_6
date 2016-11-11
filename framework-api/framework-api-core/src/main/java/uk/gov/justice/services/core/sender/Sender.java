package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Sends an action to the next layer.
 */
public interface Sender {

    /**
     * Sends envelope to the next component.  The correct sender is injected by the framework.
     *
     * @param envelope JsonEnvelope that needs to be sent.
     */
    void send(final JsonEnvelope envelope);

    /**
     * Sends envelope to the next component setting system user id.
     *
     * @param envelope JsonEnvelope that needs to be sent.
     */
    void sendAsAdmin(final JsonEnvelope envelope);
}
