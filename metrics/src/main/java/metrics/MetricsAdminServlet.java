package metrics;

import javax.servlet.annotation.WebServlet;

import com.codahale.metrics.servlets.AdminServlet;

/**
 * Annotated extension of the standard metrics admin servlet.
 */
@WebServlet(
        name = "metrics",
        value = "/internal/metrics/*"
)
public class MetricsAdminServlet extends AdminServlet {
    private static final long serialVersionUID = 8926448900805363286L;
}
