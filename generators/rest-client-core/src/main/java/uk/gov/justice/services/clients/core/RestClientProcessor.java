package uk.gov.justice.services.clients.core;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.justice.services.common.http.HeaderConstants.CAUSATION;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilderWithFilter;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;
import static uk.gov.justice.services.messaging.logging.ResponseLoggerHelper.toResponseTrace;

import uk.gov.justice.services.clients.core.exception.InvalidResponseException;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper service for processing requests for generating REST clients.
 */
public class RestClientProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientProcessor.class);

    private static final String MEDIA_TYPE_PATTERN = "application/vnd.%s+json";
    private static final String CPPID = "CPPID";
    private static final String MOCK_SERVER_PORT = "mock.server.port";
    public static final int SERVICE_NAME_POSITION = 0;
    public static final int WEB_CONTEXT_POSITION = 3;

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @Inject
    Enveloper enveloper;

    //TODO Port and appName to be removed in the near future, when WireMock library is implemented.
    private final String port;
    @Resource(lookup = "java:app/AppName")
    String appName;

    public RestClientProcessor() {
        port = System.getProperty(MOCK_SERVER_PORT);
    }

    /**
     * Make a GET request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     * @return the response from that the endpoint returned for this request
     */
    public JsonEnvelope get(final EndpointDefinition definition, final JsonEnvelope envelope) {

        final WebTarget target = createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        trace(LOGGER, () -> String.format("Sending GET request to %s using message: %s", target.getUri().toString(), envelope));

        final Response response = builder.get();

        trace(LOGGER, () -> String.format("Sent GET request %s and received: %s", envelope.metadata().id().toString(), toResponseTrace(response)));

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
                throw new RuntimeException(format("GET request %s failed; expected 200 but got %s with reason \"%s\"",
                        envelope.metadata().id().toString(), response.getStatus(), response.getStatusInfo().getReasonPhrase()));

        }

    }

    /**
     * Make a POST request using the envelope provided to a specified endpoint.
     *
     * @param definition the endpoint definition
     * @param envelope   the envelope containing the payload and/or parameters to pass in the
     *                   request
     */
    public void post(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final WebTarget target = createWebTarget(definition, envelope);

        final Builder builder = target.request(format(MEDIA_TYPE_PATTERN, definition.getResponseMediaType()));
        populateHeadersFromMetadata(builder, envelope.metadata());

        trace(LOGGER, () -> String.format("Sending POST request to %s using message: %s", target.getUri().toString(), envelope));

        final JsonObject requestBody = stripParamsFromPayload(definition, envelope);
        final Response response = builder.post(entity(requestBody.toString(), format(MEDIA_TYPE_PATTERN, envelope.metadata().name())));

        trace(LOGGER, () -> String.format("Sent POST request %s and received: %s", envelope.metadata().id().toString(), toResponseTrace(response)));

        final int status = response.getStatus();
        if (status != ACCEPTED.getStatusCode()) {
            throw new RuntimeException(format("POST request %s failed; expected 202 response but got %s with reason \"%s\"",
                    envelope.metadata().id().toString(), status,
                    response.getStatusInfo().getReasonPhrase()));
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
            builder.header(name, uuids.stream().map(id -> id.toString()).collect(joining(",")));
        }
    }

    private void setHeaderIfPresent(final Builder builder, final String name, final Optional<String> value) {
        if (value.isPresent()) {
            builder.header(name, value.get());
        }
    }

    private WebTarget createWebTarget(final EndpointDefinition definition, final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final Client client = ClientBuilder.newClient();

        WebTarget target = client
                .target(createBaseUri(definition))
                .path(definition.getPath());

        for (String pathParam : definition.getPathParams()) {
            target = target.resolveTemplate(pathParam, payload.getString(pathParam));
        }

        for (QueryParam queryParam : definition.getQueryParams()) {
            final String paramName = queryParam.getName();
            if (!payload.containsKey(paramName) && queryParam.isRequired()) {
                throw new IllegalStateException(format("Query parameter %s is required, but not present in envelope", paramName));
            }
            if (payload.containsKey(paramName)) {
                switch (queryParam.getType()) {
                    case NUMERIC:
                        target = target.queryParam(paramName, payload.getJsonNumber(paramName));
                        break;
                    case BOOLEAN:
                        target = target.queryParam(paramName, payload.getBoolean(paramName));
                        break;
                    default:
                        target = target.queryParam(paramName, payload.getString(paramName));
                }
            }
        }
        return target;
    }

    private String createBaseUri(final EndpointDefinition definition) {
        final String defaultPort = System.getProperty("DEFAULT_PORT", "8080");
        final String portToUse = isEmpty(port) || isSameService(definition) ? defaultPort : port;

        return definition.getBaseUri().replace(":8080", ":" + portToUse);
    }

    private boolean isSameService(final EndpointDefinition definition) {
        final String currentServiceName = extractServiceFromContext(appName);
        final String remoteServiceName = extractServiceFromContext(extractContextFromUri(definition.getBaseUri()));

        return currentServiceName.equals(remoteServiceName);
    }

    private String extractServiceFromContext(final String contextName) {
        return contextName.split("-")[SERVICE_NAME_POSITION];
    }

    private String extractContextFromUri(final String baseUri) {
        return baseUri.split("/")[WEB_CONTEXT_POSITION];
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
