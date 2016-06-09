package uk.gov.justice.services.adapter.rest.envelope;

import static java.util.Collections.emptyMap;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CLIENT_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;

/**
 * Utility class for building envelopes from a payload, headers, and path parameters.
 */
public class RestEnvelopeBuilder {

    private UUID id;

    private Optional<JsonObject> initialPayload = Optional.empty();
    private Optional<HttpHeaders> headers = Optional.empty();
    private Optional<Map<String, String>> params = Optional.empty();
    private String action;

    RestEnvelopeBuilder(final UUID id) {
        this.id = id;
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
     * With path and query parameters
     *
     * @param params a map of parameter names to values
     * @return an updated builder
     */
    public RestEnvelopeBuilder withParams(final Map<String, String> params) {
        this.params = Optional.of(params);
        return this;
    }

    /**
     * Define action.
     *
     * @param action name of the action
     * @return an updated builder
     */
    public RestEnvelopeBuilder withAction(final String action) {
        this.action = action;
        return this;
    }

    /**
     * Build the completed envelope.
     *
     * @return the envelope
     */
    public JsonEnvelope build() {
        return envelopeFrom(buildMetadata(), buildPayload());
    }

    private JsonObject buildPayload() {

        JsonObjectBuilder payloadBuilder = initialPayload
                .map(JsonObjects::createObjectBuilder)
                .orElse(Json.createObjectBuilder());

        for (Map.Entry<String, String> entry : params.orElse(emptyMap()).entrySet()) {
            payloadBuilder = payloadBuilder.add(entry.getKey(), entry.getValue());
        }

        return payloadBuilder.build();
    }

    private Metadata buildMetadata() {
        JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();

        metadataBuilder = metadataBuilder.add(ID, id.toString());
        metadataBuilder = metadataBuilder.add(NAME, this.action);

        if (headers.isPresent()) {
            HttpHeaders httpHeaders = headers.get();
            if (contains(CLIENT_CORRELATION_ID, httpHeaders)) {
                metadataBuilder = metadataBuilder
                        .add(CORRELATION, Json.createObjectBuilder()
                                .add(CLIENT_ID, getHeader(CLIENT_CORRELATION_ID, httpHeaders)));
            }

            if (contains(USER_ID, httpHeaders) || contains(SESSION_ID, httpHeaders)) {
                JsonObjectBuilder contextBuilder = Json.createObjectBuilder();
                if (contains(USER_ID, httpHeaders)) {
                    contextBuilder = contextBuilder.add(JsonObjectMetadata.USER_ID, getHeader(USER_ID, httpHeaders));
                }
                if (contains(SESSION_ID, httpHeaders)) {
                    contextBuilder = contextBuilder.add(JsonObjectMetadata.SESSION_ID, getHeader(SESSION_ID, httpHeaders));
                }
                metadataBuilder = metadataBuilder.add(CONTEXT, contextBuilder);
            }
        }

        return metadataFrom(metadataBuilder.build());
    }

    private boolean contains(final String header, final HttpHeaders headers) {
        return headers.getRequestHeaders() != null && headers.getRequestHeaders().containsKey(header);
    }

    private String getHeader(final String header, final HttpHeaders headers) {
        return headers.getHeaderString(header);
    }


}
