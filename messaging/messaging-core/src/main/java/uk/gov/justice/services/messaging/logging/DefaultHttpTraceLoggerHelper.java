package uk.gov.justice.services.messaging.logging;

import javax.ws.rs.core.HttpHeaders;

public class DefaultHttpTraceLoggerHelper implements HttpTraceLoggerHelper {

    @Override
    public String toHttpHeaderTrace(final HttpHeaders headers) {
        return HttpMessageLoggerHelper.toHttpHeaderTrace(headers);
    }
}