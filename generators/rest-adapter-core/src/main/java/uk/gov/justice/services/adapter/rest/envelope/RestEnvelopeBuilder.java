package uk.gov.justice.services.adapter.rest.envelope;

import static java.util.Collections.emptyList;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Collection;
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
    private Optional<Collection<Parameter>> params = Optional.empty();
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
    public RestEnvelopeBuilder withInitialPayload(final Optional<JsonObject> initialPayload) {
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
        this.headers = Optional.of(headers);
        return this;
    }

    /**
     * With path and query parameters
     *
     * @param params a map of parameter names to values
     * @return an updated builder
     */
    public RestEnvelopeBuilder withParams(final Collection<Parameter> params) {
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
        return envelopeFrom(buildMetadata(), payload());
    }

    private JsonObject payload() {

        JsonObjectBuilder payloadBuilder = initialPayload
                .map(JsonObjects::createObjectBuilder)
                .orElse(Json.createObjectBuilder());

        for (Parameter param : params.orElse(emptyList())) {
            switch (param.getType()) {
                case NUMERIC:
                    payloadBuilder = payloadBuilder.add(param.getName(), param.getNumericValue());
                    break;
                case BOOLEAN:
                    payloadBuilder = payloadBuilder.add(param.getName(), param.getBooleanValue());
                    break;
                default:
                    payloadBuilder = payloadBuilder.add(param.getName(), param.getStringValue());
            }
        }

        return payloadBuilder.build();
    }

    private Metadata buildMetadata() {
        JsonObjectMetadata.Builder metadata = metadataOf(id, this.action);

        if (headers.isPresent() && headers.get().getRequestHeaders() != null) {
            final HttpHeaders httpHeaders = this.headers.get();

            if (httpHeaders.getHeaderString(CLIENT_CORRELATION_ID) != null) {
                metadata = metadata.withClientCorrelationId(httpHeaders.getHeaderString(CLIENT_CORRELATION_ID));
            }

            if (httpHeaders.getHeaderString(USER_ID) != null) {
                metadata = metadata.withUserId(httpHeaders.getHeaderString(USER_ID));
            }
            if (httpHeaders.getHeaderString(SESSION_ID) != null) {
                metadata = metadata.withSessionId(httpHeaders.getHeaderString(SESSION_ID));
            }
        }
        return metadata.build();
    }

}
