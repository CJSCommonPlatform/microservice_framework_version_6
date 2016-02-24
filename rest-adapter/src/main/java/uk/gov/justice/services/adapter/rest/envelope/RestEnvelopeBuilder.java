package uk.gov.justice.services.adapter.rest.envelope;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static uk.gov.justice.services.adapter.rest.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.adapter.rest.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.adapter.rest.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.DefaultEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CLIENT_CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;
import static uk.gov.justice.services.messaging.Metadata.CONTEXT;
import static uk.gov.justice.services.messaging.Metadata.ID;

/**
 * Utility class for building envelopes from a payload, headers, and path parameters.
 */
public class RestEnvelopeBuilder {

    private RandomUUIDGenerator uuidGenerator;

    private Optional<JsonObject> initialPayload = Optional.empty();
    private Optional<HttpHeaders> headers = Optional.empty();
    private Optional<Map<String, String>> pathParams = Optional.empty();

    RestEnvelopeBuilder(RandomUUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }

    /**
     * With an initial payload.
     *
     * @param initialPayload the payload
     * @return an updated builder
     */
    public RestEnvelopeBuilder withInitialPayload(final JsonObject initialPayload) {
        this.initialPayload = Optional.of(initialPayload);
        return this;
    }

    /**
     * With headers.
     *
     * @param headers the headers
     * @return an updated builder
     */
    public RestEnvelopeBuilder withHeaders(final HttpHeaders headers) {
        this.headers = Optional.of(headers);
        return this;
    }

    /**
     * With path parameters
     *
     * @param pathParams a map of parameter names to values
     * @return an updated builder
     */
    public RestEnvelopeBuilder withPathParams(final Map<String, String> pathParams) {
        this.pathParams = Optional.of(pathParams);
        return this;
    }

    /**
     * Build the completed envelope.
     *
     * @return the envelope
     */
    public Envelope build() {
        return envelopeFrom(buildMetadata(), buildPayload());
    }

    private JsonObject buildPayload() {

        JsonObjectBuilder payloadBuilder = initialPayload
                .map(JsonObjects::createObjectBuilder)
                .orElse(Json.createObjectBuilder());

        final Map<String, String> params = pathParams.orElse(emptyMap());
        for (String key : params.keySet()) {
            payloadBuilder = payloadBuilder.add(key, params.get(key));
        }

        return payloadBuilder.build();
    }

    private Metadata buildMetadata() {
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();

        metadataBuilder = metadataBuilder.add(ID, uuidGenerator.generate().toString());

        HttpHeaders httpHeaders = headers.orElseThrow(() ->
                new IllegalStateException("Cannot get name from empty headers"));

        StructuredMediaType mediaType = new StructuredMediaType(httpHeaders.getMediaType());
        metadataBuilder = metadataBuilder.add(Metadata.NAME, mediaType.getName());

        if (contains(CLIENT_CORRELATION_ID, httpHeaders)) {
            metadataBuilder = metadataBuilder
                    .add(CLIENT_CORRELATION[0], Json.createObjectBuilder()
                            .add(CLIENT_CORRELATION[1], getHeader(CLIENT_CORRELATION_ID, httpHeaders)));
        }

        if (contains(USER_ID, httpHeaders) || contains(SESSION_ID, httpHeaders)) {
            JsonObjectBuilder contextBuilder = Json.createObjectBuilder();
            if (contains(USER_ID, httpHeaders)) {
                contextBuilder = contextBuilder.add(Metadata.USER_ID[1], getHeader(USER_ID, httpHeaders));
            }
            if (contains(SESSION_ID, httpHeaders)) {
                contextBuilder = contextBuilder.add(Metadata.SESSION_ID[1], getHeader(SESSION_ID, httpHeaders));
            }
            metadataBuilder = metadataBuilder.add(CONTEXT, contextBuilder);
        }

        return metadataFrom(metadataBuilder.build());
    }

    private boolean contains(final String header, final HttpHeaders headers) {
        return headers.getRequestHeaders().containsKey(header);
    }

    private String getHeader(final String header, final HttpHeaders headers) {
        return headers.getHeaderString(header);
    }
}
