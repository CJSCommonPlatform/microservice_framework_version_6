package uk.gov.justice.services.components.event.listener.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

import org.apache.commons.lang3.tuple.ImmutablePair;


public class EventListenerInterceptorChainProvider extends BaseInterceptorChainProvider {

    public EventListenerInterceptorChainProvider() {
        interceptorChainTypes().add(new ImmutablePair<>(1000, EventBufferInterceptor.class));
        interceptorChainTypes().add(new ImmutablePair<>(2000, EventFilterInterceptor.class));
    }

    @Override
    public String component() {
        return EVENT_LISTENER;
    }
}
