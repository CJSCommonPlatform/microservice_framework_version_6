package uk.gov.justice.services.event.buffer;


import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

/**
 * Interceptor to apply the Event Buffer service and stream returned events to the interceptor
 * chain.
 */
public class EventBufferInterceptor implements Interceptor {

    private static final int FIRST_CONTEXT = 0;
    private static final int EVENT_BUFFER_PRIORITY = 1000;

    @Inject
    EventBufferService eventBufferService;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {

        final List<InterceptorContext> resultContexts = interceptorChain.processNext(streamFromEventBufferFor(interceptorContext));

        if (resultContexts.isEmpty()) {
            return interceptorContext;
        }

        return resultContexts.get(FIRST_CONTEXT);
    }

    private Stream<InterceptorContext> streamFromEventBufferFor(final InterceptorContext interceptorContext) {
        return eventBufferService.currentOrderedEventsWith(interceptorContext.inputEnvelope())
                .map(interceptorContext::copyWithInput);
    }

    @Override
    public int priority() {
        return EVENT_BUFFER_PRIORITY;
    }
}
