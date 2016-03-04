package metrics;

import com.codahale.metrics.MetricRegistry;

import javax.servlet.annotation.WebListener;

import static com.codahale.metrics.servlets.MetricsServlet.ContextListener;

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
