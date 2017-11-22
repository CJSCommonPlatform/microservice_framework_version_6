package uk.gov.justice.services.core.requester;

import uk.gov.justice.services.messaging.Envelope;
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
    JsonEnvelope request(final Envelope<?> envelope);

    /**
     * Sends a request envelope to the next component.
     * The correct requester is injected by the framework.
     *
     * @param envelope envelope containing the request that needs to be sent
     * @param clazz the return Envelope payload type
     * @return an envelope containing the response with payload T
     */
    <T> Envelope<T> request(final Envelope<?> envelope, Class<T> clazz);

    /**
     * Sends a request envelope to the next component setting system user id.
     * The correct requester is injected by the framework.
     *
     * @param envelope envelope containing the request that needs to be sent
     * @return an envelope containing the response
     */
    JsonEnvelope requestAsAdmin(JsonEnvelope envelope);
}
