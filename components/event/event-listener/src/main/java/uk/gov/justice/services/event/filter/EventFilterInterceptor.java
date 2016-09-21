package uk.gov.justice.services.event.filter;


import uk.gov.justice.services.core.eventfilter.EventFilter;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import javax.inject.Inject;

public class EventFilterInterceptor implements Interceptor {

    @Inject
    private EventFilter filter;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        return filter.accepts(interceptorContext.inputEnvelope().metadata().name())
                ? interceptorChain.processNext(interceptorContext)
                : interceptorContext;
    }

    @Override
    public int priority() {
        return 1001;
    }
}