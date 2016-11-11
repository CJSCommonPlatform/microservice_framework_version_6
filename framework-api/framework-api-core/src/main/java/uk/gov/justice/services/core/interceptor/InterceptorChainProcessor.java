package uk.gov.justice.services.core.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

/**
 * Interface to start processing an InterceptorContext or JsonEnvelope.
 */
public interface InterceptorChainProcessor {

    /**
     * Start the Interceptor Chain process with the given {@link InterceptorContext}.  Can return
     * either the JsonEnvelope response from the dispatched target or {@link
     * Optional#empty()}.
     *
     * @param interceptorContext the {@link InterceptorContext} to be processed
     * @return Optional JsonEnvelope returned from the dispatched target
     */
    Optional<JsonEnvelope> process(final InterceptorContext interceptorContext);

    /**
     * Start the Interceptor Chain process with the given {@link JsonEnvelope}.  Here to support
     * backwards compatibility.  Should wrap jsonEnvelope in an {@link InterceptorContext} and call
     * {@link InterceptorChainProcessor#process(InterceptorContext)}.
     *
     * @param jsonEnvelope the {@link JsonEnvelope} to be processed
     * @return Optional JsonEnvelope returned from the dispatched target
     */
    @Deprecated
    Optional<JsonEnvelope> process(final JsonEnvelope jsonEnvelope);
}
