package uk.gov.justice.services.jmx.lifecycle;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.UnshutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.UnshutteringRequestedEvent;

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

        shutteringObserver.onShutteringRequested(new ShutteringRequestedEvent(ShutteringObserver.class.getSimpleName(), shutteringStartedAt));

        verify(logger).info("Shuttering requested started at: 2019-02-23T17:12:23Z");
        verify(shutteringBean).shutter();
    }

    @Test
    public void shouldRequestUnshutter() {

        final ZonedDateTime unShutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onUnShutteringRequested(new UnshutteringRequestedEvent(ShutteringObserver.class.getSimpleName(), unShutteringStartedAt));

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
