package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERED;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

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
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringRegistry shutteringRegistry;

    @Test
    public void shouldFireShutteringCompleteOnceAllShutterablesAreMarkedAsComplete() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);
        final ZonedDateTime now = new UtcClock().now();

        when(clock.now()).thenReturn(now);

        clearShutteringRegistry();

        shutteringRegistry.registerAsShutterable(Shutterable_1.class);
        verify(logger).info("Registering Shutterable_1 as shutterable");

        shutteringRegistry.registerAsShutterable(Shutterable_2.class);
        verify(logger).info("Registering Shutterable_2 as shutterable");

        shutteringRegistry.registerAsShutterable(Shutterable_3.class);
        verify(logger).info("Registering Shutterable_3 as shutterable");

        shutteringRegistry.shutteringStarted();

        shutteringRegistry.markShutteringCompleteFor(commandId, Shutterable_2.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_2");
        shutteringRegistry.markShutteringCompleteFor(commandId, Shutterable_1.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_1");
        shutteringRegistry.markShutteringCompleteFor(commandId, Shutterable_3.class, systemCommand);
        verify(logger).info("Marking shuttering complete for Shutterable_3");

        verify(logger).info("All shuttering complete: [Shutterable_1, Shutterable_2, Shutterable_3]");
        verify(applicationManagementStateRegistry).setApplicationManagementState(SHUTTERED);
        verify(shutteringCompleteEventFirer, times(1)).fire(new ShutteringCompleteEvent(commandId, systemCommand, now));
    }

    @Test
    public void shouldNotFireShutteringCompleteIfNotAllShutterablesAreMarkedAsComplete() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        clearShutteringRegistry();

        shutteringRegistry.registerAsShutterable(Shutterable_1.class);
        shutteringRegistry.registerAsShutterable(Shutterable_2.class);
        shutteringRegistry.registerAsShutterable(Shutterable_3.class);

        shutteringRegistry.shutteringStarted();

        shutteringRegistry.markShutteringCompleteFor(commandId, Shutterable_2.class, systemCommand);
        shutteringRegistry.markShutteringCompleteFor(commandId, Shutterable_3.class, systemCommand);

        verifyZeroInteractions(shutteringCompleteEventFirer);
    }

    private void clearShutteringRegistry() throws Exception {
        getValueOfField(shutteringRegistry, "shutteringStateMap", Map.class).clear();
    }
}

class Shutterable_1 {}
class Shutterable_2 {}
class Shutterable_3 {}
