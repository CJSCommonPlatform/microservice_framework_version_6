package uk.gov.justice.services.clients.core;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static uk.gov.justice.services.common.http.HeaderConstants.CAUSATION;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilderWithFilter;
import static uk.gov.justice.services.messaging.logging.ResponseLoggerHelper.toResponseTrace;

import uk.gov.justice.services.clients.core.exception.InvalidResponseException;
import uk.gov.justice.services.clients.core.webclient.WebTargetFactory;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import uk.gov.justice.services.core.enveloper.Enveloper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper service for processing requests for generating REST clients.
 */
public class DefaultRestClientProcessor implements RestClientProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientProcessor.class);

    private static final String MEDIA_TYPE_PATTERN = "application/vnd.%s+json";
    private static final String CPPID = "CPPID";
    private static final String CONTENT_TYPE_HEADER = "Content-type";
    private static final String PATCH = "PATCH";
    private static final String SENDING_REQUEST_MESSAGE = "Sending %s request to %s using message: %s";
    private static final String SENT_REQUEST_MESSAGE = "Sent %s request %s and received: %s";

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    Enveloper enveloper;

    @Inject
    WebTargetFactory webTargetFactory;

    @Inject
    TraceLogger traceLogger;

    /**
     * Make a synchronous GET request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    @Override
    public JsonEnvelope get(final EndpointDefinition definition, final JsonEnvelope envelope) {

        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, GET, target.getUri().toString(), envelope));

        final Response response = builder.get();

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, GET, envelope.metadata().id().toString(), toResponseTrace(response)));

        return processedResponse(envelope, response);
    }

    /**
     * Make an asynchronous POST request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    @Override
    public void post(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, POST, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder.post(entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, POST, envelope.metadata().id().toString(), toResponseTrace(response)));

        checkForAcceptedResponse(response, envelope, POST);
    }

    /**
     * Make a synchronous POST request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    @Override
    public JsonEnvelope synchronousPost(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, POST, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder.post(entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, POST, envelope.metadata().id().toString(), toResponseTrace(response)));

        return processedResponse(envelope, response);
    }

    /**
     * Make an asynchronous PUT request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    @Override
    public void put(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, PUT, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder.put(entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, PUT, envelope.metadata().id().toString(), toResponseTrace(response)));

        checkForAcceptedResponse(response, envelope, PUT);
    }

    /**
     * Make a synchronous PUT request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    @Override
    public JsonEnvelope synchronousPut(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, PUT, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder.put(entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, PUT, envelope.metadata().id().toString(), toResponseTrace(response)));

        return processedResponse(envelope, response);
    }

    /**
     * Make an asynchronous PATCH request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    @Override
    public void patch(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, PATCH, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder
                .build(PATCH, entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())))
                .invoke();

        traceLogger.trace(LOGGER, () -> format(SENT_REQUEST_MESSAGE, PATCH, envelope.metadata().id().toString(), toResponseTrace(response)));

        checkForAcceptedResponse(response, envelope, PATCH);
    }

    /**
     * Make a synchronous PATCH request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response that the endpoint returned for this request
     */
    @Override
    public JsonEnvelope synchronousPatch(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, PATCH, target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder
                .build(PATCH, entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())))
                .invoke();

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, PATCH, envelope.metadata().id().toString(), toResponseTrace(response)));

        return processedResponse(envelope, response);
    }

    /**
     * Make an asynchronous DELETE request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    @Override
    public void delete(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = webTargetFactory.createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());
        setHeaderIfPresent(builder, CONTENT_TYPE_HEADER, Optional.of(format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        traceLogger.trace(LOGGER, () -> String.format(SENDING_REQUEST_MESSAGE, DELETE, target.getUri().toString(), envelope));

        final Response response = builder.delete();

        traceLogger.trace(LOGGER, () -> String.format(SENT_REQUEST_MESSAGE, DELETE, envelope.metadata().id().toString(), toResponseTrace(response)));

        checkForAcceptedResponse(response, envelope, DELETE);
    }

    private void checkForAcceptedResponse(final Response response, final JsonEnvelope envelope, final String httpMethod) {
        final int status = response.getStatus();

        if (status != ACCEPTED.getStatusCode()) {
            throw new RuntimeException(format("%s request %s failed; expected 202 response but got %s with reason \"%s\"",
                    httpMethod,
                    envelope.metadata().id().toString(), status,
                    response.getStatusInfo().getReasonPhrase()));
        }
    }

    private JsonEnvelope processedResponse(final JsonEnvelope envelope, final Response response) {
        final Response.Status status = fromStatusCode(response.getStatus());
        switch (status) {
            case OK:
                final JsonObject responseAsJsonObject = stringToJsonObjectConverter.convert(response.readEntity(String.class));
                return jsonObjectEnvelopeConverter.asEnvelope(addMetadataIfMissing(responseAsJsonObject, envelope.metadata(), response.getHeaderString(CPPID)));
            case NOT_FOUND:
                return enveloper.withMetadataFrom(envelope, envelope.metadata().name()).apply(null);
            case FORBIDDEN:
                throw new AccessControlViolationException(response.readEntity(String.class));
            default:
                throw new RuntimeException(format("Request %s failed; expected 200 but got %s with reason \"%s\"",
                        envelope.metadata().id().toString(), response.getStatus(), response.getStatusInfo().getReasonPhrase()));
        }
    }

    private void populateHeadersFromMetadata(final Builder builder, final Metadata metadata) {
        setHeaderIfPresent(builder, CLIENT_CORRELATION_ID, metadata.clientCorrelationId());
        setHeaderIfPresent(builder, USER_ID, metadata.userId());
        setHeaderIfPresent(builder, SESSION_ID, metadata.sessionId());
        setHeaderIfPresent(builder, CAUSATION, metadata.causation());
    }

    private void setHeaderIfPresent(final Builder builder, final String name, final List<UUID> uuids) {
        if (!uuids.isEmpty()) {
            builder.header(name, uuids.stream().map(UUID::toString).collect(joining(",")));
        }
    }

    private void setHeaderIfPresent(final Builder builder, final String name, final Optional<String> value) {
        value.ifPresent(s -> builder.header(name, s));
    }

    private JsonObject stripParamsFromPayload(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final Set<String> pathParams = definition.getPathParams();
        final Set<String> queryParams = definition.getQueryParams().stream()
                .map(QueryParam::getName)
                .collect(toSet());
        return createObjectBuilderWithFilter(payload, (fieldName) -> !pathParams.contains(fieldName) && !queryParams.contains(fieldName)).build();
    }

    private JsonObject addMetadataIfMissing(final JsonObject responseAsJsonObject, final Metadata requestMetadata, final String cppId) {
        if (responseAsJsonObject.containsKey(METADATA)) {
            return responseAsJsonObject;
        }

        if (cppId == null) {
            throw new InvalidResponseException(format("Response received is missing %s header", CPPID));
        }

        final JsonObject metadata = createObjectBuilderWithFilter(requestMetadata.asJsonObject(), x -> !ID.equals(x))
                .add(ID, cppId)
                .build();

        return createObjectBuilder(responseAsJsonObject)
                .add(METADATA, metadata).build();
    }

}
