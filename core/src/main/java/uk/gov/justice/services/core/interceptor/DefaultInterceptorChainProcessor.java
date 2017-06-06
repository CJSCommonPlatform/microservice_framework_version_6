package uk.gov.justice.services.core.interceptor;

import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.function.Function;

public class DefaultInterceptorChainProcessor implements InterceptorChainProcessor {

    private final InterceptorCache interceptorCache;
    private final Function<JsonEnvelope, JsonEnvelope> dispatch;
    private final String component;

    DefaultInterceptorChainProcessor(final InterceptorCache interceptorCache, final Function<JsonEnvelope, JsonEnvelope> dispatch, final String component) {
        this.interceptorCache = interceptorCache;
        this.dispatch = dispatch;
        this.component = component;
    }

    @Override
    public Optional<JsonEnvelope> process(final InterceptorContext interceptorContext) {
        interceptorContext.setInputParameter("component", component);

        return new DefaultInterceptorChain(interceptorCache.getInterceptors(component), targetOf(dispatch))
                .processNext(interceptorContext)
                .outputEnvelope();
    }

    @Override
    @Deprecated
    public Optional<JsonEnvelope> process(final JsonEnvelope jsonEnvelope) {
        final InterceptorContext context = interceptorContextWithInput(jsonEnvelope);
        context.setInputParameter("component", component);
        return process(context);
    }

    private Target targetOf(final Function<JsonEnvelope, JsonEnvelope> dispatch) {
        return interceptorContext -> interceptorContext.copyWithOutput(dispatch.apply(interceptorContext.inputEnvelope()));
    }
}
