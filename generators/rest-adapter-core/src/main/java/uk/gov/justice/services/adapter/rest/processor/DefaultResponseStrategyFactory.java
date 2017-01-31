package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class DefaultResponseStrategyFactory implements ResponseStrategyFactory {

    @Inject
    ResponseFactoryHelper responseFactoryHelper;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    /**
     * Implementation of {@link ResponseStrategyFactory#strategyFor(String)}
     *
     * @param strategy the given strategy String
     * @return the {@link ResponseStrategy} for the given strategy type String
     * @throws IllegalArgumentException if not a recognised strategy type
     */
    @Override
    public ResponseStrategy strategyFor(final String strategy) {
        switch (strategy) {
            case OK_STATUS_WITH_ENVELOPE_ENTITY:
                return this::okStatusWithEnvelopeEntityStrategy;

            case OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY:
                return this::okStatusWithEnvelopePayloadEntityStrategy;

            case ACCEPTED_STATUS_WITH_NO_ENTITY:
                return this::acceptStatusWithNoEntityStrategy;

            default:
                throw new IllegalArgumentException(String.format("Strategy of type [%s] is not a recognised strategy type", strategy));
        }
    }

    /**
     * Uses the {@link ResponseFactoryHelper} to process the result JsonEnvelope into the
     * appropriate {@link Response}.  On OK status response, creates {@link Response} with a status
     * of OK and adds the result JsonEnvelope as the response entity.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    private Response okStatusWithEnvelopeEntityStrategy(final String action, final Optional<JsonEnvelope> result) {
        return responseFactoryHelper.responseFor(action, result,
                jsonEnvelope -> status(OK)
                        .entity(jsonObjectEnvelopeConverter.fromEnvelope(jsonEnvelope))
                        .build());
    }

    /**
     * Uses the {@link ResponseFactoryHelper} to process the result JsonEnvelope into the
     * appropriate Http {@link Response}.  On OK status response, creates {@link Response} with a
     * status of OK, adds the metadata id of the result JsonEnvelope to the header as CCPID and adds
     * the payload of the result JsonEnvelope as the response entity.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    private Response okStatusWithEnvelopePayloadEntityStrategy(final String action, final Optional<JsonEnvelope> result) {
        return responseFactoryHelper.responseFor(action, result,
                jsonEnvelope -> status(OK)
                        .header(ID, jsonEnvelope.metadata().id())
                        .entity(jsonEnvelope.payloadAsJsonObject())
                        .build());
    }

    /**
     * Create {@link Response} with Accepted status, with no entity payload.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    private Response acceptStatusWithNoEntityStrategy(final String action, final Optional<JsonEnvelope> result) {
        return status(ACCEPTED).build();
    }
}