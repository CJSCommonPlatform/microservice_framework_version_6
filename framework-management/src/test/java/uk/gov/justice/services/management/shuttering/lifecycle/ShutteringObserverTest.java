package uk.gov.justice.services.management.shuttering.lifecycle;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringObserverTest {

    @Mock
    private Logger logger;

    @Mock
    private ShutteringBean shutteringBean;

    @InjectMocks
    private ShutteringObserver shutteringObserver;

    @Test
    public void shouldRequestShutter() {

        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        shutteringObserver.onShutteringRequested(new ShutteringRequestedEvent(systemCommand, shutteringStartedAt));

        verify(logger).info("Shuttering requested started at: 2019-02-23T17:12:23Z");
        verify(shutteringBean).shutter();
    }

    @Test
    public void shouldRequestUnshutter() {

        final ZonedDateTime unShutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);
        final SystemCommand systemCommand = mock(SystemCommand.class);

        shutteringObserver.onUnShutteringRequested(new UnshutteringRequestedEvent(systemCommand, unShutteringStartedAt));

        verify(logger).info("Unshuttering requested started at: 2019-02-23T17:12:23Z");
        verify(shutteringBean).unshutter();
    }

    @Test
    public void shouldInformShutteringCompleted() {

        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onShutteringComplete(new ShutteringCompleteEvent(shutteringStartedAt));

        verify(logger).info("Shuttering completed at: 2019-02-23T17:12:23Z");
    }

    @Test
    public void shouldInformUnshutteringCompleted() {

        final ZonedDateTime unshutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onUnshutteringComplete(new UnshutteringCompleteEvent(unshutteringStartedAt));

        verify(logger).info("Unshuttering completed at: 2019-02-23T17:12:23Z");
    }
}
