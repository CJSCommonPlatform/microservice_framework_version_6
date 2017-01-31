package uk.gov.justice.services.adapter.rest.envelope;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static uk.gov.justice.services.common.http.HeaderConstants.CAUSATION;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilderWithFilter;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonObject;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;

/**
 * Utility class for building envelopes from a payload, headers, and path parameters.
 */
public class RestEnvelopeBuilder {

    private static final String METADATA_AND_HEADER_ARE_SET = "The metadata of payload and the headers both have %s set and the values are not equal: payload = %s, headers = %s";

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
        final JsonObjectBuilder payloadBuilder = initialPayload
                .map(jsonObject -> createObjectBuilderWithFilter(jsonObject, key -> !key.equals(METADATA)))
                .orElse(Json.createObjectBuilder());

        params.ifPresent(parameters ->
                parameters.forEach(param -> {
                            switch (param.getType()) {
                                case NUMERIC:
                                    payloadBuilder.add(param.getName(), param.getNumericValue());
                                    break;
                                case BOOLEAN:
                                    payloadBuilder.add(param.getName(), param.getBooleanValue());
                                    break;
                                default:
                                    payloadBuilder.add(param.getName(), param.getStringValue());
                            }
                        }
                ));

        return payloadBuilder.build();
    }

    private Metadata buildMetadata() {
        final Optional<Metadata> payloadMetadata = metadataFromPayloadIfPresent();
        final JsonObjectMetadata.Builder metadataBuilder = payloadMetadata
                .map(JsonObjectMetadata::metadataFrom)
                .orElse(metadataOf(id, this.action));

        return mergeHeadersIntoMetadata(payloadMetadata, metadataBuilder).build();
    }

    private Optional<Metadata> metadataFromPayloadIfPresent() {
        return initialPayload
                .flatMap(jsonObject -> getJsonObject(jsonObject, METADATA))
                .map(JsonObjectMetadata::metadataFrom);
    }

    private JsonObjectMetadata.Builder mergeHeadersIntoMetadata(final Optional<Metadata> metadata, final JsonObjectMetadata.Builder metadataBuilder) {
        final Optional<String> correlationId;
        final Optional<String> sessionId;
        final Optional<String> userId;
        final List<UUID> causation;

        if (metadata.isPresent()) {
            final Metadata metadataValue = metadata.get();

            correlationId = metadataValue.clientCorrelationId();
            sessionId = metadataValue.sessionId();
            userId = metadataValue.userId();
            causation = metadataValue.causation();
        } else {
            correlationId = Optional.empty();
            sessionId = Optional.empty();
            userId = Optional.empty();
            causation = emptyList();
        }

        final boolean requestHeadersArePresent = headers.isPresent() && headers.get().getRequestHeaders() != null;

        if (requestHeadersArePresent) {
            final HttpHeaders httpHeaders = this.headers.get();

            setMetaDataIfNotSet(
                    correlationId,
                    httpHeaders.getHeaderString(CLIENT_CORRELATION_ID),
                    metadataBuilder::withClientCorrelationId,
                    "Client Correlation Id");

            setMetaDataIfNotSet(
                    userId,
                    httpHeaders.getHeaderString(USER_ID),
                    metadataBuilder::withUserId,
                    "User Id");

            setMetaDataIfNotSet(
                    sessionId,
                    httpHeaders.getHeaderString(SESSION_ID),
                    metadataBuilder::withSessionId,
                    "Session Id");

            setCausationMetaDataIfNotSet(
                    causation,
                    httpHeaders.getHeaderString(CAUSATION),
                    metadataBuilder::withCausation);
        }

        return metadataBuilder;
    }

    private void setMetaDataIfNotSet(final Optional<String> metadataValue,
                                     final String headerValue,
                                     final Consumer<String> setMetadata,
                                     final String exceptionInfo) {

        final boolean valueIsPresentAndNotEqualInHeaderAndPayload = metadataValue.isPresent()
                && headerValue != null
                && !metadataValue.get().equals(headerValue);

        if (valueIsPresentAndNotEqualInHeaderAndPayload) {
            throw new BadRequestException(format(METADATA_AND_HEADER_ARE_SET, exceptionInfo, metadataValue.get(), headerValue));
        } else if (headerValue != null) {
            setMetadata.accept(headerValue);
        }
    }

    private void setCausationMetaDataIfNotSet(final List<UUID> metadataValue,
                                              final String headerValue,
                                              final Consumer<UUID[]> setMetadata) {

        if (headerValue != null) {
            final List<UUID> uuids = stream(headerValue.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            final boolean valueIsPresentAndNotEqualInHeaderAndPayload = !metadataValue.isEmpty()
                    && !metadataValue.equals(uuids);

            if (valueIsPresentAndNotEqualInHeaderAndPayload) {
                throw new BadRequestException(format(METADATA_AND_HEADER_ARE_SET, "Causation", metadataValue, headerValue));
            } else {
                setMetadata.accept(uuids.toArray(new UUID[uuids.size()]));
            }
        }
    }
}
