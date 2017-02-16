package uk.gov.justice.services.adapter.rest.processor;

import javax.ws.rs.core.Response;

public interface ResponseStrategyFactory {

    /**
     * Strategy type to return a {@link Response} with OK status, with resulting JsonEnvelope as the
     * returned entity.
     */
    String OK_STATUS_WITH_ENVELOPE_ENTITY = "OK_STATUS_WITH_ENVELOPE_ENTITY";

    /**
     * Strategy type to return a {@link Response} with OK status, with resulting JsonEnvelope
     * payload as the returned entity.
     */
    String OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY = "OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY";

    /**
     * Strategy type to return a {@link Response} with Accepted status, with no entity payload.
     */
    String ACCEPTED_STATUS_WITH_NO_ENTITY = "ACCEPTED_STATUS_WITH_NO_ENTITY";

    /**
     * Create {@link ResponseStrategy} for given strategy type.
     * Possible strategies are:
     * {@link ResponseStrategyFactory#OK_STATUS_WITH_ENVELOPE_ENTITY}
     * {@link ResponseStrategyFactory#OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY}
     * {@link ResponseStrategyFactory#ACCEPTED_STATUS_WITH_NO_ENTITY}
     *
     * @param strategy the given strategy String
     * @return the {@link ResponseStrategy} for the given strategy type String
     */
    ResponseStrategy strategyFor(final String strategy);
}
