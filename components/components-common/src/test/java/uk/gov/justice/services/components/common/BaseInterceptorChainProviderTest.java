package uk.gov.justice.services.components.common;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.List;

import org.junit.Test;

public class BaseInterceptorChainProviderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProvideDefaultInterceptorChainTypes() throws Exception {
        final List<InterceptorChainEntry> interceptorChainTypes = new TestInterceptorChainProvider().interceptorChainTypes();

        assertThat(interceptorChainTypes, containsInAnyOrder(
                new InterceptorChainEntry(1, TotalActionMetricsInterceptor.class),
                new InterceptorChainEntry(2, IndividualActionMetricsInterceptor.class)));
    }

    public static class TestInterceptorChainProvider extends BaseInterceptorChainProvider {

        @Override
        public String component() {
            return "Test Component";
        }
    }
}
