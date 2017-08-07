package uk.gov.justice.services.messaging.logging;

import javax.ws.rs.core.HttpHeaders;

public interface HttpTraceLoggerHelper {

    String toHttpHeaderTrace(final HttpHeaders headers);
}