package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.UnshutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class UnshutterCommandApiObserverTest {

    @Mock
    private CommandApiShutteringBean commandApiShutteringBean;

    @Mock
    private UnshutteringRegistry unshutteringRegistry;

    @Mock
    private Logger logger;

    @InjectMocks
    private UnshutterCommandApiObserver unshutterCommandApiObserver;

    @Test
    public void shouldUnshutterTheCommandApiAndFireTheUnshutteringCompleteEvent() throws Exception {

        final SystemCommand systemCommand = mock(SystemCommand.class);

        final UnshutteringProcessStartedEvent unshutteringProcessStartedEvent = new UnshutteringProcessStartedEvent(
                systemCommand,
                new UtcClock().now()
        );

        unshutterCommandApiObserver.onUnshutteringProcessStarted(unshutteringProcessStartedEvent);

        final InOrder inOrder = inOrder(logger, commandApiShutteringBean, unshutteringRegistry);

        inOrder.verify(logger).info("Unshuttering Command API");
        inOrder.verify(commandApiShutteringBean).unshutter();
        inOrder.verify(logger).info("Unshuttering of Command API complete");
        inOrder.verify(unshutteringRegistry).markUnshutteringCompleteFor(UnshutterCommandApiObserver.class, systemCommand);
    }
}
