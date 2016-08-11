package uk.gov.justice.services.core.interceptor;

import static uk.gov.justice.services.core.interceptor.InterceptorContext.copyWithOutput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

/**
 * Target calls to dispatch.
 */
public class DispatcherTarget implements Target {

    private Function<JsonEnvelope, JsonEnvelope> dispatcher;

    public DispatcherTarget(final Function<JsonEnvelope, JsonEnvelope> dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Calls the dispatcher with the input JsonEnvelope from the interceptor context.
     *
     * @param interceptorContext the context that contains the input JsonEnvelope
     * @return the a new interceptor context that contains the output from the dispatcher
     */
    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext) {
        return copyWithOutput(interceptorContext, dispatcher.apply(interceptorContext.inputEnvelope()));
    }
}
