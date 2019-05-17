package uk.gov.justice.services.jmx;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RebuildTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @InjectMocks
    private Rebuild rebuild;

    @Test
    public void shouldFireRebuildRequestedEvent() {

        final ZonedDateTime requestedAt = new UtcClock().now();

        when(clock.now()).thenReturn(requestedAt);

        rebuild.doRebuildRequested();

        verify(rebuildRequestedEventEventFirer).fire(new RebuildRequestedEvent(Rebuild.class.getSimpleName(), requestedAt));
    }
}