package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Sends an request to the next layer.
 */
@FunctionalInterface
public interface Requester {

    /**
     * Sends a request envelope to the next component. The correct requester is injected by the
     * framework.
     *
     * @param envelope envelope containing the request that needs to be sent
     * @return an envelope containing the response
     */
    JsonEnvelope request(final JsonEnvelope envelope);
}
