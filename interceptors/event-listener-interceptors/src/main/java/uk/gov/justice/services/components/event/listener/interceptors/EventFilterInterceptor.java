package uk.gov.justice.services.components.event.listener.interceptors;


import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventFilter;

import javax.inject.Inject;

/**
 * @deprecated This class should no longer be used, EventFilterInterceptor is specifically generated
 * for Event Listeners from the RAML.
 */
@Deprecated
public class EventFilterInterceptor implements Interceptor {

    @Inject
    private EventFilter filter;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        if (filter.accepts(interceptorContext.inputEnvelope().metadata().name())) {
            return interceptorChain.processNext(interceptorContext);
        }

        return interceptorContext;
    }
}
