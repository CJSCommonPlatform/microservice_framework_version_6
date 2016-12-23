package uk.gov.justice.services.adapter.rest.filter;

import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.adapter.rest.envelope.MediaTypes.JSON_MEDIA_TYPE_SUFFIX;
import static uk.gov.justice.services.adapter.rest.envelope.MediaTypes.charsetFrom;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;
import static uk.gov.justice.services.common.log.LoggerConstants.REQUEST_DATA;
import static uk.gov.justice.services.messaging.logging.JmsMessageLoggerHelper.addServiceContextNameIfPresent;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Filter listens to all requests and adds request information to the Logger Mapped Diagnostic
 * Context.  This can be added to the log output by setting %X{frameworkRequestData} in the logger
 * pattern.
 */
@Provider
public class LoggerRequestDataFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    Logger logger;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Inject
    StringToJsonObjectConverter stringToJsonObjectConverter;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        trace(logger, () -> "Adding request data to MDC");

        final JsonObjectBuilder builder = createObjectBuilder();
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        addServiceContextNameIfPresent(serviceContextNameProvider, builder);
        addContentTypeAndAcceptIfPresent(builder, headers);
        mergeHeadersWithPayloadMetadataIfPresent(requestContext, headers)
                .ifPresent(metadataBuilder -> builder.add(METADATA, metadataBuilder));

        MDC.put(REQUEST_DATA, builder.build().toString());

        trace(logger, () -> "Request data added to MDC");
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        trace(logger, () -> "Clearing MDC");
        MDC.clear();
    }

    private void addContentTypeAndAcceptIfPresent(final JsonObjectBuilder builder, final MultivaluedMap<String, String> headers) {
        Optional.ofNullable(headers.getFirst(CONTENT_TYPE)).ifPresent(value -> builder.add(CONTENT_TYPE, value));
        Optional.ofNullable(headers.getFirst(ACCEPT)).ifPresent(value -> builder.add(ACCEPT, value));
    }

    private Optional<JsonObject> mergeHeadersWithPayloadMetadataIfPresent(final ContainerRequestContext requestContext,
                                                                          final MultivaluedMap<String, String> headers) throws IOException {
        final Optional<Metadata> payloadMetadata = payloadMetadataIfPresent(requestContext);

        final Optional<String> id = idFromHeaderOrMetadataIfPresent(headers, payloadMetadata);
        final Optional<String> name = nameFromHeaderOrMetadataIfPresent(headers, payloadMetadata);
        final Optional<String> correlationId = correlationIdFromHeaderOrMetadataIfPresent(headers, payloadMetadata);
        final Optional<String> sessionId = sessionIdFromHeaderOrMetadataIfPresent(headers, payloadMetadata);
        final Optional<String> userId = userIdFromHeaderOrMetadataIfPresent(headers, payloadMetadata);

        if (id.isPresent() || name.isPresent() || correlationId.isPresent() || sessionId.isPresent() || userId.isPresent()) {
            final JsonObjectBuilder metadataBuilder = createObjectBuilder();

            id.ifPresent(value -> metadataBuilder.add("id", value));
            name.ifPresent(value -> metadataBuilder.add("name", value));
            correlationId.ifPresent(value -> metadataBuilder.add("correlation", createObjectBuilder().add("client", value)));

            if (sessionId.isPresent() || userId.isPresent()) {
                final JsonObjectBuilder contextBuilder = createObjectBuilder();

                sessionId.ifPresent(value -> contextBuilder.add("session", value));
                userId.ifPresent(value -> contextBuilder.add("user", value));

                metadataBuilder.add("context", contextBuilder);
            }

            return Optional.of(metadataBuilder.build());
        }

        return Optional.empty();
    }

    private Optional<Metadata> payloadMetadataIfPresent(final ContainerRequestContext requestContext) throws IOException {
        final Optional<MediaType> mediaType = Optional.ofNullable(requestContext.getMediaType());

        if (isPayloadPresent(mediaType)) {
            final String charset = charsetFrom(mediaType.get());
            final String payload = IOUtils.toString(requestContext.getEntityStream(), charset);

            try {
                if (isNotBlank(payload)) {
                    final JsonObject jsonObjectPayload = stringToJsonObjectConverter.convert(payload);
                    if (jsonObjectPayload. containsKey(JsonEnvelope.METADATA)) {
                        return Optional.of(payloadMetadataFrom(jsonObjectPayload));
                    }
                }

            } finally {
                requestContext.setEntityStream(new ByteArrayInputStream(payload.getBytes(charset)));
            }
        }

        return Optional.empty();
    }

    private Optional<String> idFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers, final Optional<Metadata> payloadMetadata) {
        return getFromHeaderOrMetadataIfPresent(headers, payloadMetadata, ID, metadata -> Optional.of(metadata.id().toString()));
    }

    private Optional<String> nameFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers, final Optional<Metadata> payloadMetadata) {
        return getFromHeaderOrMetadataIfPresent(headers, payloadMetadata, NAME, metadata -> Optional.of(metadata.name()));
    }

    private Optional<String> correlationIdFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers, final Optional<Metadata> payloadMetadata) {
        return getFromHeaderOrMetadataIfPresent(headers, payloadMetadata, CLIENT_CORRELATION_ID, Metadata::clientCorrelationId);
    }

    private Optional<String> sessionIdFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers, final Optional<Metadata> payloadMetadata) {
        return getFromHeaderOrMetadataIfPresent(headers, payloadMetadata, SESSION_ID, Metadata::sessionId);
    }

    private Optional<String> userIdFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers, final Optional<Metadata> payloadMetadata) {
        return getFromHeaderOrMetadataIfPresent(headers, payloadMetadata, USER_ID, Metadata::userId);
    }

    private Optional<String> getFromHeaderOrMetadataIfPresent(final MultivaluedMap<String, String> headers,
                                                              final Optional<Metadata> payloadMetadata,
                                                              final String headerConstant,
                                                              final Function<Metadata, Optional<String>> fromMetadata) {
        return Optional.ofNullable(headers.getFirst(headerConstant))
                .map(Optional::of)
                .orElse(payloadMetadata
                        .flatMap(fromMetadata));
    }

    private boolean isPayloadPresent(final Optional<MediaType> mediaType) {
        return mediaType.map(mediaTypeValue ->
                null != mediaTypeValue.getSubtype() && mediaTypeValue.getSubtype().endsWith(JSON_MEDIA_TYPE_SUFFIX))
                .orElse(false);
    }

    private Metadata payloadMetadataFrom(final JsonObject payload) {
        return new JsonObjectEnvelopeConverter()
                .asEnvelope(payload)
                .metadata();
    }
}
