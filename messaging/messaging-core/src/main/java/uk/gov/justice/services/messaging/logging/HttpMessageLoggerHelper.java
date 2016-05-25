package uk.gov.justice.services.messaging.logging;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.util.Optional;

import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

public final class HttpMessageLoggerHelper {

    private HttpMessageLoggerHelper() {}

    public static String toHttpHeaderTrace(final HttpHeaders headers) {

        final JsonObjectBuilder builder = Json.createObjectBuilder();

        final Optional<MediaType> mediaType = Optional.ofNullable(headers.getMediaType());

        mediaType.ifPresent(s -> {
            builder.add("MediaType", mediaType.get().toString());
        });

        addHeader(builder, headers, ID);
        addHeader(builder, headers, CLIENT_CORRELATION_ID);
        addHeader(builder, headers, SESSION_ID);
        addHeader(builder, headers, NAME);
        addHeader(builder, headers, USER_ID);

        return builder.build().toString();
    }

    private static void addHeader(final JsonObjectBuilder builder, final HttpHeaders headers, final String headerKey) {

        if(contains(headerKey, headers)) {
            builder.add(headerKey, getHeader(headerKey, headers));
        }
    }

    private static boolean contains(final String header, final HttpHeaders headers) {
        return headers.getRequestHeaders() != null && headers.getRequestHeaders().containsKey(header);
    }

    private static String getHeader(final String header, final HttpHeaders headers) {
        return headers.getHeaderString(header);
    }
}
