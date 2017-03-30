package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

/**
 * Interface to start processing an InterceptorContext or JsonEnvelope.
 */
public interface InterceptorChainProcessor {

    Optional<JsonEnvelope> process(final InterceptorContext interceptorContext);

    @Deprecated
    Optional<JsonEnvelope> process(final JsonEnvelope jsonEnvelope);
}