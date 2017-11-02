package uk.gov.justice.services.components.event.listener.interceptors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.List;

import org.junit.Test;

public class EventListenerInterceptorChainProviderTest {

    @Test
    public void shouldReturnComponent() throws Exception {
        assertThat(new EventListenerInterceptorChainProvider().component(), is(EVENT_LISTENER));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvideDefaultInterceptorChainTypes() throws Exception {
        final List<InterceptorChainEntry> interceptorChainTypes = new EventListenerInterceptorChainProvider().interceptorChainTypes();

        assertThat(interceptorChainTypes, containsInAnyOrder(
                new InterceptorChainEntry(1, TotalActionMetricsInterceptor.class),
                new InterceptorChainEntry(2, IndividualActionMetricsInterceptor.class),
                new InterceptorChainEntry(1000, EventBufferInterceptor.class),
                new InterceptorChainEntry(2000, EventFilterInterceptor.class)));
    }
}
