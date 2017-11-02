package uk.gov.justice.services.components.event.listener.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;


public class EventListenerInterceptorChainProvider extends BaseInterceptorChainProvider {

    public EventListenerInterceptorChainProvider() {
        interceptorChainTypes().add(new InterceptorChainEntry(1000, EventBufferInterceptor.class));
        interceptorChainTypes().add(new InterceptorChainEntry(2000, EventFilterInterceptor.class));
    }

    @Override
    public String component() {
        return EVENT_LISTENER;
    }
}
