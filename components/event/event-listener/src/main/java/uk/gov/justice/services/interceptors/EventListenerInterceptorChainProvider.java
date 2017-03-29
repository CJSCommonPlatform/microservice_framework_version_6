package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.event.buffer.EventBufferInterceptor;
import uk.gov.justice.services.event.filter.EventFilterInterceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class EventListenerInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_LISTENER;
    }

    @Override
    public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
        final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = super.interceptorChainTypes();
        interceptorChainTypes.add(new ImmutablePair<>(1000, EventBufferInterceptor.class));
        interceptorChainTypes.add(new ImmutablePair<>(2000, EventFilterInterceptor.class));
        return interceptorChainTypes;
    }
}