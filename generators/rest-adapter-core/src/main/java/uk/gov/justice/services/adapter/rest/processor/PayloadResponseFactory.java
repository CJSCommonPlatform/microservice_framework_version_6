package uk.gov.justice.services.adapter.rest.processor;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Create {@link Response} with OK status, with resulting JsonEnvelope payload as the returned
 * entity.
 */
public class PayloadResponseFactory implements ResponseFactory {

    @Inject
    ResponseFactoryHelper responseFactoryHelper;

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
    @Override
    public Response responseFor(final String action, final Optional<JsonEnvelope> result) {
        return responseFactoryHelper.responseFor(action, result,
                jsonEnvelope -> status(OK)
                        .header(ID, jsonEnvelope.metadata().id())
                        .entity(jsonEnvelope.payloadAsJsonObject())
                        .build());
    }
}