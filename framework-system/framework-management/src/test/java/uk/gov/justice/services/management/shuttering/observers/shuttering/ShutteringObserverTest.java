package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringObserverTest {

    @Mock
    private Event<ShutteringProcessStartedEvent> shutteringProcessStartedEventFirer;

    @Mock
    private ShutteringRegistry shutteringRegistry;

    @Mock
    private Logger logger;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private ShutteringObserver shutteringObserver;

    @Test
    public void shouldRegisterShutteringStartedAndFireShutteringProcessStartedEventOnShutteringRequested() {

        final String commandName = "CATCHUP";
        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand target = mock(SystemCommand.class);

        when(target.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(shutteringStartedAt);
        shutteringObserver.onShutteringRequested(new ShutteringRequestedEvent(target, shutteringStartedAt));

        final InOrder inOrder = inOrder(logger, shutteringRegistry, shutteringProcessStartedEventFirer);

        inOrder.verify(logger).info("Shuttering requested for CATCHUP at: 2019-02-23T17:12:23Z");
        inOrder.verify(shutteringRegistry).shutteringStarted();
        inOrder.verify(shutteringProcessStartedEventFirer).fire(new ShutteringProcessStartedEvent(target, shutteringStartedAt));
    }

    @Test
    public void shouldInformShutteringCompleted() {

        final String commandName = "CATCHUP";
        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand target = mock(SystemCommand.class);

        when(target.getName()).thenReturn(commandName);

        shutteringObserver.onShutteringComplete(new ShutteringCompleteEvent(target, shutteringStartedAt));

        verify(logger).info("Shuttering completed for CATCHUP at: 2019-02-23T17:12:23Z");
    }
}
