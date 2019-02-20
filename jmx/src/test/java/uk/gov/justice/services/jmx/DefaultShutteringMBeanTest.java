package uk.gov.justice.services.jmx;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringRequestedEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultShutteringMBeanTest {

    @Mock
    private ApplicationStateController applicationStateController;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private DefaultShutteringMBean defaultShutteringMBean;

    @Test
    public void shouldCreateInstanceOfShutteringMBean() {
        final DefaultShutteringMBean defaultShutteringMBean = new DefaultShutteringMBean();
        assertThat(defaultShutteringMBean, instanceOf(ShutteringMBean.class));
    }

    @Test
    public void shouldFireShutteringRequestedEvent() {
        defaultShutteringMBean.doShutteringRequested();
        verify(applicationStateController).fireShutteringRequested(any(ShutteringRequestedEvent.class));
    }

    @Test
    public void shouldFireUnshutteringRequestedEvent() {
        defaultShutteringMBean.doUnshutteringRequested();
        verify(applicationStateController).fireUnshutteringRequested(any(UnshutteringRequestedEvent.class));
    }


}