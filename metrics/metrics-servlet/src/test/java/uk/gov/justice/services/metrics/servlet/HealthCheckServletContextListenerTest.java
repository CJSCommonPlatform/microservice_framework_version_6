package uk.gov.justice.services.metrics.servlet;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import javax.servlet.annotation.WebListener;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the{@link HealthCheckServletContextListener} class.
 */
public class HealthCheckServletContextListenerTest {

    private HealthCheckServletContextListener listener;

    @Before
    public void setup() {
        listener = new HealthCheckServletContextListener();
    }

    @Test
    public void shouldReturnAHealthCheckRegistry() throws Exception {
        assertThat(listener.getHealthCheckRegistry(), notNullValue());
    }

    @Test
    public void shouldBeAWebListener() {
        WebListener annotation = listener.getClass().getAnnotation(WebListener.class);
        assertThat(annotation, notNullValue());
    }
}
