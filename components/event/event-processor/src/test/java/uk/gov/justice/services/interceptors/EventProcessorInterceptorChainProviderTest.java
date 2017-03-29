package uk.gov.justice.services.interceptors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.accesscontrol.LocalAccessControlInterceptor;
import uk.gov.justice.services.core.audit.LocalAuditInterceptor;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.metrics.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.core.metrics.TotalActionMetricsInterceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class EventProcessorInterceptorChainProviderTest {

    @Test
    public void shouldReturnComponent() throws Exception {
        assertThat(new EventProcessorInterceptorChainProvider().component(), is(EVENT_PROCESSOR));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvideDefaultInterceptorChainTypes() throws Exception {
        final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = new EventProcessorInterceptorChainProvider().interceptorChainTypes();

        assertThat(interceptorChainTypes, containsInAnyOrder(
                new ImmutablePair<>(1, TotalActionMetricsInterceptor.class),
                new ImmutablePair<>(2, IndividualActionMetricsInterceptor.class),
                new ImmutablePair<>(3000, LocalAuditInterceptor.class),
                new ImmutablePair<>(4000, LocalAccessControlInterceptor.class)));
    }
}