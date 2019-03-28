package uk.gov.justice.services.jmx;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CatchupTest {

    @Mock
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private Catchup catchup;

    @Test
    public void shouldFireCatchupRequestedEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(requestedAt);

        catchup.doCatchupRequested();
        verify(catchupRequestedEventFirer).fire(new CatchupRequestedEvent(catchup, requestedAt));
    }
}
