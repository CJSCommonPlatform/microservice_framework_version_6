package uk.gov.justice.services.jmx.lifecycle;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;

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
    private ShutteringFlagProducerBean shutteringFlagProducerBean;

    @InjectMocks
    private ShutteringObserver shutteringObserver;

    @Test
    public void onShutterringRequested() {

        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onShutterringRequested(new ShutteringRequestedEvent(ShutteringObserver.class.getSimpleName(), shutteringStartedAt));

        verify(logger).info("Shuttering requested started at: 2019-02-23T17:12:23Z");
        verify(shutteringFlagProducerBean).setDoShuttering(true);
    }

    @Test
    public void onUnShutterringRequested() {

        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onUnShutterringRequested(new ShutteringCompleteEvent(shutteringStartedAt));

        verify(logger).info("Unshuttering requested started at: 2019-02-23T17:12:23Z");
        verify(shutteringFlagProducerBean).setDoShuttering(false);
    }
}
