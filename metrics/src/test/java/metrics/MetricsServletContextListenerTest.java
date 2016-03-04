package metrics;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.annotation.WebListener;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the{@link MetricsServletContextListener} class.
 */
public class MetricsServletContextListenerTest {

    private MetricsServletContextListener listener;

    @Before
    public void setup() {
        listener = new MetricsServletContextListener();
    }

    @Test
    public void shouldReturnAMetricRegistry() throws Exception {
        assertThat(listener.getMetricRegistry(), notNullValue());
    }

    @Test
    public void shouldBeAWebListener() {
        WebListener annotation = listener.getClass().getAnnotation(WebListener.class);
        assertThat(annotation, notNullValue());
    }
}
