package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

/**
 * Functional interface for the chain process this represents both asynchronous and synchronous
 * function calls.
 */
@FunctionalInterface
public interface InterceptorChainProcessor {

    Optional<JsonEnvelope> process(final InterceptorContext interceptorContext);
}