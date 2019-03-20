package uk.gov.justice.services.jmx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringRequestedEvent;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringTest {

    @Mock
    private ApplicationStateController applicationStateController;

    @Mock
    private UtcClock utcClock;

    @InjectMocks
    private Shuttering shuttering;

    @Test
    public void shouldCreateInstanceOfShutteringMBean() {
        final Shuttering shuttering = new Shuttering();
        assertThat(shuttering, instanceOf(ShutteringMBean.class));
    }

    @Test
    public void shouldFireShutteringRequestedEvent() {
        shuttering.doShutteringRequested();
        verify(applicationStateController).fireShutteringRequested(any(ShutteringRequestedEvent.class));
    }

    @Test
    public void shouldFireUnshutteringRequestedEvent() {
        shuttering.doUnshutteringRequested();
        verify(applicationStateController).fireUnshutteringRequested(any(UnshutteringRequestedEvent.class));
    }
}
