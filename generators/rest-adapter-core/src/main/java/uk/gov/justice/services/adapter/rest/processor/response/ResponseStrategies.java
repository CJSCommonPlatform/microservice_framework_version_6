package uk.gov.justice.services.adapter.rest.processor.response;

import javax.ws.rs.core.Response;

/**
 * Named response strategies for REST generator
 */
final public class ResponseStrategies {

    /**
     * Strategy to return a {@link Response} with Accepted status, with no entity payload.
     */
    public static final String ACCEPTED_STATUS_NO_ENTITY_RESPONSE_STRATEGY = "AcceptedStatusNoEntityResponseStrategy";

    /**
     * Strategy to return a {@link Response} with OK status, with resulting JsonEnvelope as the
     * returned entity.
     */
    public static final String OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY = "OkStatusEnvelopeEntityResponseStrategy";

    /**
     * Strategy to return a {@link Response} with OK status, with resulting JsonEnvelope
     * payload as the returned entity.
     */
    public static final String OK_STATUS_ENVELOPE_PAYLOAD_ENTITY_RESPONSE_STRATEGY = "OkStatusEnvelopePayloadEntityResponseStrategy";

    private ResponseStrategies() {
    }
}