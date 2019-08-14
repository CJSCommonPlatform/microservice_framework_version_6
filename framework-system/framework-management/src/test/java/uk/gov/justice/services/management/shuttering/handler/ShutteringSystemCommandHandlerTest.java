package uk.gov.justice.services.management.shuttering.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_UNSHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_STARTED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERING_STARTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ShutterSystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.observers.ContextShutteredStateObserver;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringSystemCommandHandlerTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Mock
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Mock
    private ContextShutteredStateObserver contextShutteredStateObserver;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringSystemCommandHandler shutteringSystemCommandHandler;

    @Captor
    private ArgumentCaptor<ShutteringRequestedEvent> shutteringEventCaptor;

    @Captor
    private ArgumentCaptor<UnshutteringRequestedEvent> unshutteringEventCaptor;

    @Test
    public void shouldFireEventOnShutteringRequested() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final ShutterSystemCommand shutterSystemCommand = new ShutterSystemCommand();

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(CONTEXT_UNSHUTTERED);
        when(clock.now()).thenReturn(now);

        shutteringSystemCommandHandler.onShutterRequested(shutterSystemCommand);

        verify(shutteringRequestedEventFirer).fire(shutteringEventCaptor.capture());

        final ShutteringRequestedEvent shutteringRequestedEvent = shutteringEventCaptor.getValue();

        assertThat(shutteringRequestedEvent.getTarget(), is(shutterSystemCommand));
        assertThat(shutteringRequestedEvent.getShutteringRequestedAt(), is(now));
    }

    @Test
    public void shouldFireEventOnUnshutteringRequested() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final UnshutterSystemCommand unshutterSystemCommand = new UnshutterSystemCommand();

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(CONTEXT_SHUTTERED);
        when(clock.now()).thenReturn(now);

        shutteringSystemCommandHandler.onUnshutterRequested(unshutterSystemCommand);

        verify(unshutteringRequestedEventFirer).fire(unshutteringEventCaptor.capture());

        final UnshutteringRequestedEvent unshutteringRequestedEvent = unshutteringEventCaptor.getValue();

        assertThat(unshutteringRequestedEvent.getTarget(), is(unshutterSystemCommand));
        assertThat(unshutteringRequestedEvent.getUnshutteringRequestedAt(), is(now));
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsContextShuttered() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(CONTEXT_SHUTTERED);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'CONTEXT_SHUTTERED'");
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsShutteringStarted() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(SHUTTERING_STARTED);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'SHUTTERING_STARTED'");
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsUnshutteringStarted() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(UNSHUTTERING_STARTED);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'UNSHUTTERING_STARTED'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsContextUnshuttered() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(CONTEXT_UNSHUTTERED);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'CONTEXT_UNSHUTTERED'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsShutteringStarted() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(SHUTTERING_STARTED);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'SHUTTERING_STARTED'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsUnshutteringStarted() throws Exception {

        when(contextShutteredStateObserver.getShutteredState()).thenReturn(UNSHUTTERING_STARTED);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterSystemCommand());

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'UNSHUTTERING_STARTED'");
    }
}
