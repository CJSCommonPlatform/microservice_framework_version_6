package uk.gov.justice.services.jmx;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupRequestedEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultCatchupMBeanTest {

    @Mock
    private ApplicationStateController applicationStateController;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private DefaultCatchupMBean defaultCatchupMBean;

    @Test
    public void shouldCreteInstanceOfCatchupMBean() {
        final DefaultCatchupMBean defaultCatchupMBean = new DefaultCatchupMBean();
        assertThat(defaultCatchupMBean, instanceOf(CatchupMBean.class));
    }

    @Test
    public void shouldFireCatchupRequestedEvent() {
        defaultCatchupMBean.doCatchupRequested();
        verify(applicationStateController).fireCatchupRequested(any(CatchupRequestedEvent.class));
    }
}