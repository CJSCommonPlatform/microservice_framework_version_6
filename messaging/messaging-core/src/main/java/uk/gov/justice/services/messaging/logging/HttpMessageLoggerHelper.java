package uk.gov.justice.services.messaging.logging;

import javax.inject.Inject;
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
