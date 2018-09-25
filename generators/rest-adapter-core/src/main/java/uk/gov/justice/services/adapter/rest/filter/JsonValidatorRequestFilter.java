package uk.gov.justice.services.adapter.rest.filter;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.Priorities.USER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.services.adapter.rest.envelope.MediaTypes.JSON_MEDIA_TYPE_SUFFIX;
import static uk.gov.justice.services.adapter.rest.envelope.MediaTypes.charsetFrom;
import static uk.gov.justice.services.adapter.rest.filter.JsonValidatorRequestFilter.FILTER_PRIORITY;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.messaging.logging.HttpTraceLoggerHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

/**
 * Filter that validates any JSON payload data in the request. Throws a BadRequestException if the payload
 * has a JSON media type but fails to parse into valid JSON. Note validation does not involve JSON Schema,
 * it is purely to check that the data is of a valid JSON format. This filter needs to run before
 * LoggerRequestDataFilter as LoggerRequestDataFilter results in a HTTP 500 error code if it attempts to process invalid JSON
 */
@Priority(FILTER_PRIORITY)
@Provider
public class JsonValidatorRequestFilter implements ContainerRequestFilter {

    public static final int FILTER_PRIORITY = USER - 100;

    @Inject
    Logger logger;

    @Inject
    HttpTraceLoggerHelper httpTraceLoggerHelper;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        trace(logger, () -> "Validating JSON");
        validateJsonPayloadIfPresent(requestContext);
    }


    private void validateJsonPayloadIfPresent(final ContainerRequestContext requestContext) throws IOException {
        final Optional<MediaType> mediaType = Optional.ofNullable(requestContext.getMediaType());

        if (isPayloadPresent(mediaType)) {
            final String charset = mediaType.isPresent() ? charsetFrom(mediaType.get()) : defaultCharset().name();
            final String payload = IOUtils.toString(requestContext.getEntityStream(), charset);

            try {
                if (isNotBlank(payload)) {
                    objectMapper.readTree(payload);
                }
            }
            catch (final JsonProcessingException jsonEx) {
                final String message = format("Invalid JSON provided to [%s] JSON: [%s] ",
                        httpTraceLoggerHelper.toHttpHeaderTrace(stripUserId(requestContext.getHeaders())),
                        payload);
                trace(logger, () -> message);
                throw new BadRequestException(message);
            }

            requestContext.setEntityStream(new ByteArrayInputStream(payload.getBytes(charset)));
        }

    }

    private boolean isPayloadPresent(final Optional<MediaType> mediaType) {
        return mediaType.map(mediaTypeValue ->
                null != mediaTypeValue.getSubtype() && mediaTypeValue.getSubtype().endsWith(JSON_MEDIA_TYPE_SUFFIX))
                .orElse(false);
    }

    private MultivaluedMap<String, String> stripUserId(final MultivaluedMap<String, String> headers) {
        return new MultivaluedHashMap<>(headers.entrySet()
                   .stream()
                   .filter(e -> !USER_ID.equals(e.getKey()))
                   .collect(toMap(e -> e.getKey(), e -> e.getValue().get(0))));

    }
}
