package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class UnshutteringRegistryTest {

    @Mock
    private Event<UnshutteringCompleteEvent> unshutteringCompleteEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private UnshutteringRegistry unshutteringRegistry;

    @Test
    public void shouldFireUnshutteringCompleteOnceAllUnshutterablesAreMarkedAsComplete() throws Exception {

        final String commandName = "CATCHUP";
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ZonedDateTime now = new UtcClock().now();

        when(clock.now()).thenReturn(now);
        when(systemCommand.getName()).thenReturn(commandName);

        unshutteringRegistry.clear();

        unshutteringRegistry.registerAsUnshutterable(Unshutterable_1.class);
        verify(logger).info("Registering Unshutterable_1 as unshutterable");

        unshutteringRegistry.registerAsUnshutterable(Unshutterable_2.class);
        verify(logger).info("Registering Unshutterable_2 as unshutterable");

        unshutteringRegistry.registerAsUnshutterable(Unshutterable_3.class);
        verify(logger).info("Registering Unshutterable_3 as unshutterable");

        unshutteringRegistry.unshutteringStarted();

        unshutteringRegistry.markUnshutteringCompleteFor(Unshutterable_2.class, systemCommand);
        unshutteringRegistry.markUnshutteringCompleteFor(Unshutterable_1.class, systemCommand);
        unshutteringRegistry.markUnshutteringCompleteFor(Unshutterable_3.class, systemCommand);

        verify(unshutteringCompleteEventFirer, times(1)).fire(new UnshutteringCompleteEvent(systemCommand, now));
    }

    @Test
    public void shouldNotFireUnshutteringCompleteIfNotAllUnshutterablesAreMarkedAsComplete() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);

        unshutteringRegistry.clear();

        unshutteringRegistry.registerAsUnshutterable(Unshutterable_1.class);
        unshutteringRegistry.registerAsUnshutterable(Unshutterable_2.class);
        unshutteringRegistry.registerAsUnshutterable(Unshutterable_3.class);

        unshutteringRegistry.unshutteringStarted();

        unshutteringRegistry.markUnshutteringCompleteFor(Unshutterable_2.class, systemCommand);
        unshutteringRegistry.markUnshutteringCompleteFor(Unshutterable_3.class, systemCommand);

        verifyZeroInteractions(unshutteringCompleteEventFirer);
    }
}

class Unshutterable_1 {}

class Unshutterable_2 {}

class Unshutterable_3 {}
