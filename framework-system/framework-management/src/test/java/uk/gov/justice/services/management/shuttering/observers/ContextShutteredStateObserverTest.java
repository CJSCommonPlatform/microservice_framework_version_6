package uk.gov.justice.services.management.shuttering.observers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_UNSHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_STARTED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERING_STARTED;

import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import org.junit.Test;

public class ContextShutteredStateObserverTest {

    @Test
    public void shouldBeUnshutteredWhenClassInstantiated() throws Exception {

        assertThat(new ContextShutteredStateObserver().getShutteredState(), is(CONTEXT_UNSHUTTERED));
    }

    @Test
    public void shouldCorrectlySetState() throws Exception {

        final ContextShutteredStateObserver contextShutteredStateObserver = new ContextShutteredStateObserver();
        final ShutteringRequestedEvent shutteringRequestedEvent = mock(ShutteringRequestedEvent.class);
        final ShutteringCompleteEvent shutteringCompleteEvent = mock(ShutteringCompleteEvent.class);
        final UnshutteringRequestedEvent unshutteringRequestedEvent = mock(UnshutteringRequestedEvent.class);
        final UnshutteringCompleteEvent unshutteringCompleteEvent = mock(UnshutteringCompleteEvent.class);

        assertThat(contextShutteredStateObserver.getShutteredState(), is(CONTEXT_UNSHUTTERED));

        contextShutteredStateObserver.onShutteringRequested(shutteringRequestedEvent);
        assertThat(contextShutteredStateObserver.getShutteredState(), is(SHUTTERING_STARTED));

        contextShutteredStateObserver.onShutteringComplete(shutteringCompleteEvent);
        assertThat(contextShutteredStateObserver.getShutteredState(), is(CONTEXT_SHUTTERED));

        contextShutteredStateObserver.onUnshutteringRequested(unshutteringRequestedEvent);
        assertThat(contextShutteredStateObserver.getShutteredState(), is(UNSHUTTERING_STARTED));

        contextShutteredStateObserver.onUnshutteringComplete(unshutteringCompleteEvent);
        assertThat(contextShutteredStateObserver.getShutteredState(), is(CONTEXT_UNSHUTTERED));
    }
}
