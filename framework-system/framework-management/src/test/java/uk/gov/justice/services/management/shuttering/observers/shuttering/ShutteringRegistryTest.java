package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringRegistryTest {

    @Mock
    private Event<ShutteringCompleteEvent> shutteringCompleteEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringRegistry shutteringRegistry;

    @Test
    public void shouldFireShutteringCompleteOnceAllShutterablesAreMarkedAsComplete() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ZonedDateTime now = new UtcClock().now();

        when(clock.now()).thenReturn(now);

        shutteringRegistry.clear();

        shutteringRegistry.registerAsShutterable(Shutterable_1.class);
        verify(logger).info("Registering Shutterable_1 as shutterable");

        shutteringRegistry.registerAsShutterable(Shutterable_2.class);
        verify(logger).info("Registering Shutterable_2 as shutterable");

        shutteringRegistry.registerAsShutterable(Shutterable_3.class);
        verify(logger).info("Registering Shutterable_3 as shutterable");

        shutteringRegistry.shutteringStarted();

        shutteringRegistry.markShutteringCompleteFor(Shutterable_2.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_2");
        shutteringRegistry.markShutteringCompleteFor(Shutterable_1.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_1");
        shutteringRegistry.markShutteringCompleteFor(Shutterable_3.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_3");

        verify(logger).info("All shuttering complete: [Shutterable_1, Shutterable_2, Shutterable_3]");
        verify(shutteringCompleteEventFirer, times(1)).fire(new ShutteringCompleteEvent(systemCommand, now));
    }

    @Test
    public void shouldNotFireShutteringCompleteIfNotAllShutterablesAreMarkedAsComplete() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);

        shutteringRegistry.clear();

        shutteringRegistry.registerAsShutterable(Shutterable_1.class);
        shutteringRegistry.registerAsShutterable(Shutterable_2.class);
        shutteringRegistry.registerAsShutterable(Shutterable_3.class);

        shutteringRegistry.shutteringStarted();

        shutteringRegistry.markShutteringCompleteFor(Shutterable_2.class, systemCommand);
        shutteringRegistry.markShutteringCompleteFor(Shutterable_3.class, systemCommand);

        verifyZeroInteractions(shutteringCompleteEventFirer);
    }
}

class Shutterable_1 {}
class Shutterable_2 {}
class Shutterable_3 {}
