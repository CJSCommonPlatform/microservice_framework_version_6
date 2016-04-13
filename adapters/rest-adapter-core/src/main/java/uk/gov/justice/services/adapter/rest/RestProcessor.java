package uk.gov.justice.services.adapter.rest;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * In order to minimise the amount of generated code in the JAX-RS implementation classes, this service encapsulates
 * all the logic for building an envelope from the REST request, passing it to a consumer and building a suitable
 * response. This allows testing of this logic independently from the automated generation code.
 */
public class RestProcessor {

    @Inject
    RestEnvelopeBuilderFactory envelopeBuilderFactory;

    /**
     * Process an incoming REST request by combining the payload, headers and path parameters into an envelope and
     * passing the envelope to the given consumer.
     *
     * @param consumer       a consumer for the envelope
     * @param initialPayload the payload from the REST request
     * @param headers        the headers from the REST request
     * @param pathParams     the path parameters from the REST request
     * @return the HTTP response to return to the client
     */
    public Response processAsynchronously(final Consumer<JsonEnvelope> consumer,
                                          final JsonObject initialPayload,
                                          final HttpHeaders headers,
                                          final Map<String, String> pathParams) {

        JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withHeaders(headers)
                .withPathParams(pathParams)
                .build();

        consumer.accept(envelope);

        return Response.status(ACCEPTED).build();
    }

    public Response processSynchronously(final Function<JsonEnvelope, JsonEnvelope> function,
                                         final HttpHeaders headers,
                                         final Map<String, String> pathParams) {
        JsonEnvelope envelope = envelopeBuilderFactory.builder()
                .withHeaders(headers)
                .withPathParams(pathParams)
                .build();

        JsonEnvelope result = function.apply(envelope);
        Response.ResponseBuilder response =
                result != null ? Response.status(OK).entity(result.payload().toString()) : Response.status(NOT_FOUND);
        return response.build();
    }
}
