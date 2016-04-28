package metrics;

import static com.codahale.metrics.servlets.MetricsServlet.ContextListener;

import javax.servlet.annotation.WebListener;

import com.codahale.metrics.MetricRegistry;

/**
 * Annotated context listener for wiring up the metrics servlet.
 */
@WebListener
public class MetricsServletContextListener extends ContextListener {

    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    @Override
    protected MetricRegistry getMetricRegistry() {
        return METRIC_REGISTRY;
    }

}
