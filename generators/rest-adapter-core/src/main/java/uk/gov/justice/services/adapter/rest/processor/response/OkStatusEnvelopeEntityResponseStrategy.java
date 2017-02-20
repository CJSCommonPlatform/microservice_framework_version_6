package uk.gov.justice.services.adapter.rest.processor.response;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.adapter.rest.processor.response.ResponseStrategies.OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Named(OK_STATUS_ENVELOPE_ENTITY_RESPONSE_STRATEGY)
public class OkStatusEnvelopeEntityResponseStrategy implements ResponseStrategy {

    @Inject
    ResponseStrategyHelper responseStrategyHelper;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    /**
     * Uses the {@link ResponseStrategyHelper} to process the result JsonEnvelope into the
     * appropriate {@link Response}.  On OK status response, creates {@link Response} with a status
     * of OK and adds the result JsonEnvelope as the response entity.
     *
     * @param action the action being processed
     * @param result the resulting JsonEnvelope
     * @return the {@link Response} to return from the REST call
     */
    @Override
    public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
        return responseStrategyHelper.responseFor(action, result,
                jsonEnvelope -> status(OK)
                        .entity(jsonObjectEnvelopeConverter.fromEnvelope(jsonEnvelope))
                        .build());
    }
}