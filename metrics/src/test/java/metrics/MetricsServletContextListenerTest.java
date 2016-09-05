package metrics;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

public class MetricsServletContextListenerTest {

    @Test
    public void shouldReturnMetricsRegistry() throws Exception {

        MetricRegistry registry = new MetricRegistry();
        MetricsServletContextListener listener = new MetricsServletContextListener();
        listener.metricRegistry = registry;

        assertThat(listener.getMetricRegistry(), is(registry));
    }
}