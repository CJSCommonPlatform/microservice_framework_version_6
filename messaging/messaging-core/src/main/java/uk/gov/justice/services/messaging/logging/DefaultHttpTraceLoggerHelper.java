package uk.gov.justice.services.messaging.logging;

import static java.lang.String.join;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.common.http.HeaderConstants.NAME;
import static uk.gov.justice.services.common.http.HeaderConstants.SESSION_ID;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.common.log.LoggerConstants.METADATA;

import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

public class DefaultHttpTraceLoggerHelper implements HttpTraceLoggerHelper {

    @Override
    public String toHttpHeaderTrace(final HttpHeaders headers) {
        return toHttpHeaderTrace(headers.getRequestHeaders());
    }

    @Override
    public String toHttpHeaderTrace(final MultivaluedMap<String, String> headers){
        return addHttpHeadersToJsonBuilder(headers, createObjectBuilder()).build().toString();

    }

    private JsonObjectBuilder addHttpHeadersToJsonBuilder(final MultivaluedMap<String, String> headers, final JsonObjectBuilder builder) {
        addHeader(builder, headers, CONTENT_TYPE);
        addHeader(builder, headers, ACCEPT);

        final JsonObjectBuilder metadataBuilder = createObjectBuilder();
        addHeader(metadataBuilder, headers, ID);
        addHeader(metadataBuilder, headers, CLIENT_CORRELATION_ID);
        addHeader(metadataBuilder, headers, SESSION_ID);
        addHeader(metadataBuilder, headers, NAME);
        addHeader(metadataBuilder, headers, USER_ID);
        builder.add(METADATA, metadataBuilder.build());

        return builder;
    }


    private void addHeader(final JsonObjectBuilder builder,
                                  final MultivaluedMap<String, String> headers,
                                  final String headerKey) {

        if (contains(headerKey, headers)) {
            builder.add(headerKey, getHeader(headerKey, headers));
        }
    }

    private boolean contains(final String header, final MultivaluedMap<String, String> headers) {
        return headers.containsKey(header);
    }

    private String getHeader(final String header, final MultivaluedMap<String, String> headers) {
        return join(",", headers.get(header));
    }
}