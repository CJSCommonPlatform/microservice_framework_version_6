package uk.gov.justice.services.adapter.rest;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;

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
     * @param consumer a consumer for the envelope
     * @param initialPayload the payload from the REST request
     * @param headers the headers from the REST request
     * @param pathParams the path parameters from the REST request
     * @return the HTTP response to return to the client
     */
    public Response process(final Consumer<Envelope> consumer,
                            final JsonObject initialPayload,
                            final HttpHeaders headers,
                            final Map<String, String> pathParams) {

        Envelope envelope = envelopeBuilderFactory.builder()
                .withInitialPayload(initialPayload)
                .withHeaders(headers)
                .withPathParams(pathParams)
                .build();

        consumer.accept(envelope);

        return Response.status(202).build();
    }
}
