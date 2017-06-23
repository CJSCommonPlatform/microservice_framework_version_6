package uk.gov.justice.services.components.event.listener.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;
import uk.gov.justice.services.core.accesscontrol.LocalAccessControlInterceptor;
import uk.gov.justice.services.core.audit.LocalAuditInterceptor;
import uk.gov.justice.services.core.interceptor.Interceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


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
