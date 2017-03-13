package uk.gov.justice.services.core.interceptor;

import static java.lang.String.format;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.copyWithOutput;
import static uk.gov.justice.services.messaging.logging.LoggerUtils.trace;

import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class InterceptorChainProcessorProducer {

    @Inject
    Logger logger;

    @Inject
    DispatcherCache dispatcherCache;

    @Inject
    InterceptorCache interceptorCache;

    /**
     * Produces an interceptor chain processor for the provided injection point.
     *
     * @param injectionPoint class where the {@link InterceptorChainProcessor} is being injected
     * @return the interceptor chain processor
     */
    @Produces
    public InterceptorChainProcessor produceProcessor(final InjectionPoint injectionPoint) {
        final Function<JsonEnvelope, JsonEnvelope> dispatch = dispatcherCache.dispatcherFor(injectionPoint)::dispatch;

        trace(logger, () -> format("Interceptor Chain Processor provided for %s", injectionPoint.getClass().getName()));

        return createProcessor(dispatch);
    }

    /**
     * Constructs the {@link InterceptorChainProcessor} for the given dispatch interceptor target
     * and injection point.
     *
     * @param dispatch the dispatch target method
     * @return the interceptor chain processor function
     */
    private InterceptorChainProcessor createProcessor(final Function<JsonEnvelope, JsonEnvelope> dispatch) {

        return interceptorContext -> {
            final InterceptorChain interceptorChain = new InterceptorChain(interceptorCache.getInterceptors(), targetOf(dispatch));

            return interceptorChain
                    .processNext(interceptorContext)
                    .outputEnvelope();
        };
    }

    private Target targetOf(final Function<JsonEnvelope, JsonEnvelope> dispatch) {
        return interceptorContext -> copyWithOutput(interceptorContext, dispatch.apply(interceptorContext.inputEnvelope()));
    }
}
