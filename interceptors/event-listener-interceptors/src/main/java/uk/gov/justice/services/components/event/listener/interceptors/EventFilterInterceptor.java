package uk.gov.justice.services.components.event.listener.interceptors;


import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventFilter;
import uk.gov.justice.services.event.buffer.api.EventFilterRegistry;

import javax.inject.Inject;

public class EventFilterInterceptor implements Interceptor {

    @Inject
    private EventFilterRegistry filter;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        return filter.accepts(interceptorContext.inputEnvelope().metadata().name())
                ? interceptorChain.processNext(interceptorContext)
                : interceptorContext;
    }
}
