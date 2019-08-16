package uk.gov.justice.services.management.shuttering.observers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERING_IN_PROGRESS;

import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationShutteredStateObserverTest {

    @Mock
    private ShutteringStateRegistry shutteringStateRegistry;

    @InjectMocks
    private ApplicationShutteredStateObserver contextShutteredStateObserver;

    @Test
    public void shouldSetShutteringInProgressOnShutteringRequested() throws Exception {

        final ShutteringRequestedEvent shutteringRequestedEvent = mock(ShutteringRequestedEvent.class);

        contextShutteredStateObserver.onShutteringRequested(shutteringRequestedEvent);

        verify(shutteringStateRegistry).setShutteredState(SHUTTERING_IN_PROGRESS);
    }

    @Test
    public void shouldSetShutteringInProgressOnShutteringCompete() throws Exception {

        final ShutteringCompleteEvent shutteringCompleteEvent = mock(ShutteringCompleteEvent.class);

        contextShutteredStateObserver.onShutteringComplete(shutteringCompleteEvent);

        verify(shutteringStateRegistry).setShutteredState(SHUTTERED);
    }

    @Test
    public void shouldSetShutteringInProgressOnUnshutteringRequested() throws Exception {

        final UnshutteringRequestedEvent unshutteringRequestedEvent = mock(UnshutteringRequestedEvent.class);

        contextShutteredStateObserver.onUnshutteringRequested(unshutteringRequestedEvent);

        verify(shutteringStateRegistry).setShutteredState(UNSHUTTERING_IN_PROGRESS);
    }

    @Test
    public void shouldSetShutteringInProgressOnUnshutteringComplete() throws Exception {

        final UnshutteringCompleteEvent unshutteringCompleteEvent = mock(UnshutteringCompleteEvent.class);

        contextShutteredStateObserver.onUnshutteringComplete(unshutteringCompleteEvent);

        verify(shutteringStateRegistry).setShutteredState(UNSHUTTERED);
    }
}
