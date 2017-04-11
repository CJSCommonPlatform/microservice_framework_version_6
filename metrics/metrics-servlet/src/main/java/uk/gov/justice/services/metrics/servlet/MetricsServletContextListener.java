package uk.gov.justice.services.metrics.servlet;

import static com.codahale.metrics.servlets.MetricsServlet.ContextListener;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;

import com.codahale.metrics.MetricRegistry;

/**
 * Annotated context listener for wiring up the metrics servlet.
 */
@WebListener
public class MetricsServletContextListener extends ContextListener {

    @Inject
    MetricRegistry metricRegistry;

    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

}
