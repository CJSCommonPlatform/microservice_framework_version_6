package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Sends an request to the next layer.
 */
@FunctionalInterface
public interface Requester {

    /**
     * Sends a request jsonEnvelope to the next component. The correct requester is injected by the
     * framework.
     *
     * @param jsonEnvelope jsonEnvelope containing the request that needs to be sent
     * @return an jsonEnvelope containing the response
     */
    JsonEnvelope request(final JsonEnvelope jsonEnvelope);
}
