package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Functional interface for the chain process this represents both asynchronous and synchronous
 * function calls.
 */
@FunctionalInterface
public interface InterceptorChainProcessor {

    JsonEnvelope process(final JsonEnvelope envelope);
}