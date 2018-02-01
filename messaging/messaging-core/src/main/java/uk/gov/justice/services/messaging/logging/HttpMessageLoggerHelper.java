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

import javax.inject.Inject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @deprecated Use injected httpTraceLoggerHelper.toHttpHeaderTrace(HttpHeaders headers)
 * and httpTraceLoggerHelper.toHttpHeaderTrace(MultivaluedMap<String, String> headers)
 */
public final class HttpMessageLoggerHelper {

    @Inject
    HttpTraceLoggerHelper httpTraceLoggerHelper;

    private HttpMessageLoggerHelper() {
    }

    /**
     * @deprecated Use httpTraceLoggerHelper.toHttpHeaderTrace(HttpHeaders headers)
     */
    @Deprecated
    public static String toHttpHeaderTrace(final HttpHeaders headers) {
        return new DefaultHttpTraceLoggerHelper().toHttpHeaderTrace(headers);
    }

    /**
     * @deprecated Use httpTraceLoggerHelper.toHttpHeaderTrace(MultivaluedMap<String, String> headers)
     */
    @Deprecated
    public static String toHttpHeaderTrace(final MultivaluedMap<String, String> headers) {
        return new DefaultHttpTraceLoggerHelper().toHttpHeaderTrace(headers);
    }

}
