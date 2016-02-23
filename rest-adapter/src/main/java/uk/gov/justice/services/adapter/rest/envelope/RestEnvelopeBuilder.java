package uk.gov.justice.services.adapter.rest.envelope;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import java.util.Map;

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

    private JsonObject initialPayload;
    private HttpHeaders headers;
    private Map<String, String> pathParams;

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
        this.initialPayload = initialPayload;
        return this;
    }

    /**
     * With headers.
     *
     * @param headers the headers
     * @return an updated builder
     */
    public RestEnvelopeBuilder withHeaders(final HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * With path parameters
     *
     * @param pathParams a map of parameter names to values
     * @return an updated builder
     */
    public RestEnvelopeBuilder withPathParams(final Map<String, String> pathParams) {
        this.pathParams = pathParams;
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
        JsonObjectBuilder payloadBuilder = initialPayload == null ?
                Json.createObjectBuilder() : JsonObjects.createObjectBuilder(initialPayload);

        if (pathParams != null) {
            for (String key : pathParams.keySet()) {
                payloadBuilder = payloadBuilder.add(key, pathParams.get(key));
            }
        }

        return payloadBuilder.build();
    }

    private Metadata buildMetadata() {
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();

        metadataBuilder = metadataBuilder.add(ID, uuidGenerator.generate().toString());

        if (contains(CLIENT_CORRELATION_ID)) {
            metadataBuilder = metadataBuilder
                    .add(CLIENT_CORRELATION[0], Json.createObjectBuilder()
                            .add(CLIENT_CORRELATION[1], getHeader(CLIENT_CORRELATION_ID)));
        }

        if (headers == null) {
            throw new IllegalStateException("Cannot get name from null headers");
        }
        StructuredMediaType mediaType = new StructuredMediaType(headers.getMediaType());
        metadataBuilder = metadataBuilder.add(Metadata.NAME, mediaType.getName());

        if (contains(USER_ID) || contains(SESSION_ID)) {
            JsonObjectBuilder contextBuilder = Json.createObjectBuilder();
            if (contains(USER_ID)) {
                contextBuilder = contextBuilder.add(Metadata.USER_ID[1], getHeader(USER_ID));
            }
            if (contains(SESSION_ID)) {
                contextBuilder = contextBuilder.add(Metadata.SESSION_ID[1], getHeader(SESSION_ID));
            }
            metadataBuilder = metadataBuilder.add(CONTEXT, contextBuilder);
        }
        return metadataFrom(metadataBuilder.build());
    }

    private boolean contains(final String header) {
        return headers != null && headers.getRequestHeaders().containsKey(header);
    }

    private String getHeader(final String header) {
        return headers.getHeaderString(header);
    }
}
