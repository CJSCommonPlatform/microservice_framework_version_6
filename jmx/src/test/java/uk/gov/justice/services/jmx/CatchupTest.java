package uk.gov.justice.services.jmx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupRequestedEvent;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CatchupTest {
    @Mock
    private ApplicationStateController applicationStateController;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private Catchup catchup;

    @Test
    public void shouldCreteInstanceOfCatchupMBean() {
        final Catchup catchup = new Catchup();
        assertThat(catchup, instanceOf(CatchupMBean.class));
    }

    @Test
    public void shouldFireCatchupRequestedEvent() {
        catchup.doCatchupRequested();
        verify(applicationStateController).fireCatchupRequested(any(CatchupRequestedEvent.class));
    }
}