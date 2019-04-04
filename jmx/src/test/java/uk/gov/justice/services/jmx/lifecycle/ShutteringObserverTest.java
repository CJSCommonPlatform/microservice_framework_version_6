package uk.gov.justice.services.jmx.lifecycle;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;
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
    private ShutteringFlagProducerBean shutteringFlagProducerBean;

    @InjectMocks
    private ShutteringObserver shutteringObserver;

    @Test
    public void onShutterringRequested() {

        final ZonedDateTime shutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onShutterringRequested(new ShutteringRequestedEvent(ShutteringObserver.class.getSimpleName(), shutteringStartedAt));

        verify(logger).info(format("Shuttering requested started at: %s", "2019-02-23T17:12:23Z"));
        verify(shutteringFlagProducerBean).setDoShuttering(true);
    }

    @Test
    public void onUnShutterringRequested() {

        final ZonedDateTime unShutteringStartedAt = of(2019, 2, 23, 17, 12, 23, 0, UTC);

        shutteringObserver.onUnShutterringRequested(new UnshutteringRequestedEvent(ShutteringObserver.class.getSimpleName(), unShutteringStartedAt));

        verify(logger).info(format("Unshuttering requested started at: %s", "2019-02-23T17:12:23Z"));
        verify(shutteringFlagProducerBean).setDoShuttering(false);
    }
}
