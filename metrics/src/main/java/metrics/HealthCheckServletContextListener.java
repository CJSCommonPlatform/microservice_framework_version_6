package metrics;

import static com.codahale.metrics.servlets.HealthCheckServlet.ContextListener;

import javax.servlet.annotation.WebListener;

import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * Annotated context listener for wiring up the health check servlet.
 */
@WebListener
public class HealthCheckServletContextListener extends ContextListener {

    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return HEALTH_CHECK_REGISTRY;
    }

}
