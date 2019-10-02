package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class UnshutteringObserverTest {

    @Mock
    private Event<UnshutteringProcessStartedEvent> unshutteringProcessStartedEventFirer;

    @Mock
    private UnshutteringRegistry unshutteringRegistry;

    @Mock
    private Logger logger;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private UnshutteringObserver unshutteringObserver;

    @Test
    public void shouldRegisterUnshutteringStartedAndFireUnshutteringProcessStartedEventOnUnshutteringRequested() {

        final UUID commandId = randomUUID();
        final String commandName = "CATCHUP";
        final ZonedDateTime unshutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand target = mock(SystemCommand.class);

        when(target.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(unshutteringStartedAt);
        unshutteringObserver.onUnshutteringRequested(new UnshutteringRequestedEvent(commandId, target, unshutteringStartedAt));

        final InOrder inOrder = inOrder(logger, unshutteringRegistry, unshutteringProcessStartedEventFirer);

        inOrder.verify(logger).info("Unshuttering requested for CATCHUP at: 2019-02-23T17:12:23Z");
        inOrder.verify(unshutteringRegistry).unshutteringStarted();
        inOrder.verify(unshutteringProcessStartedEventFirer).fire(new UnshutteringProcessStartedEvent(commandId, target, unshutteringStartedAt));
    }

    @Test
    public void shouldInformUnshutteringCompleted() {

        final UUID commandId = randomUUID();
        final String commandName = "CATCHUP";
        final ZonedDateTime unshutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand target = mock(SystemCommand.class);

        when(target.getName()).thenReturn(commandName);

        unshutteringObserver.onUnshutteringComplete(new UnshutteringCompleteEvent(commandId, target, unshutteringStartedAt));

        verify(logger).info("Unshuttering completed for CATCHUP at: 2019-02-23T17:12:23Z");
    }
}
