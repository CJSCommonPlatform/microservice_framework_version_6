package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutterCommandApiObserverTest {

    @Mock
    private CommandApiShutteringBean commandApiShutteringBean;

    @Mock
    private ShutteringRegistry shutteringRegistry;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutterCommandApiObserver shutterCommandApiObserver;

    @Test
    public void shouldShutterTheCommandApiAndFireTheShutteringCompleteEvent() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);

        final ShutteringProcessStartedEvent shutteringProcessStartedEvent = new ShutteringProcessStartedEvent(
                systemCommand,
                new UtcClock().now()
        );

        shutterCommandApiObserver.onShutteringProcessStarted(shutteringProcessStartedEvent);

        final InOrder inOrder = inOrder(logger, commandApiShutteringBean, shutteringRegistry);

        inOrder.verify(logger).info("Shuttering Command API");
        inOrder.verify(commandApiShutteringBean).shutter();
        inOrder.verify(logger).info("Shuttering of Command API complete");
        inOrder.verify(shutteringRegistry).markShutteringCompleteFor(ShutterCommandApiObserver.class, systemCommand);
    }
}
