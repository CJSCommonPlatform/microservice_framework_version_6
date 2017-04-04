package uk.gov.justice.services.core.requester;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Sends an request to the next layer.
 */
public interface Requester {

    /**
     * Sends a request envelope to the next component.
     * The correct requester is injected by the framework.
     *
     * @param envelope envelope containing the request that needs to be sent
     * @return an envelope containing the response
     */
    JsonEnvelope request(final JsonEnvelope envelope);

    /**
     * Sends a request envelope to the next component setting system user id.
     * The correct requester is injected by the framework.
     *
     * @param envelope envelope containing the request that needs to be sent
     * @return an envelope containing the response
     */
    JsonEnvelope requestAsAdmin(JsonEnvelope envelope);
}
