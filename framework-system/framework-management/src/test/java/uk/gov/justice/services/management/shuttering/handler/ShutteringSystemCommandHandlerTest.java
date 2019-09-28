package uk.gov.justice.services.management.shuttering.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERED;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERED;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERING_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

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
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

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

        final UUID commandId = randomUUID();
        final ZonedDateTime now = new UtcClock().now();
        final ShutterCommand shutterCommand = new ShutterCommand();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(UNSHUTTERED);
        when(clock.now()).thenReturn(now);

        shutteringSystemCommandHandler.onShutterRequested(shutterCommand, commandId);

        verify(applicationManagementStateRegistry).setApplicationManagementState(SHUTTERING_IN_PROGRESS);
        verify(shutteringRequestedEventFirer).fire(shutteringEventCaptor.capture());

        final ShutteringRequestedEvent shutteringRequestedEvent = shutteringEventCaptor.getValue();

        assertThat(shutteringRequestedEvent.getTarget(), is(shutterCommand));
        assertThat(shutteringRequestedEvent.getShutteringRequestedAt(), is(now));
    }

    @Test
    public void shouldFireEventOnUnshutteringRequested() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime now = new UtcClock().now();
        final UnshutterCommand unshutterCommand = new UnshutterCommand();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(SHUTTERED);
        when(clock.now()).thenReturn(now);

        shutteringSystemCommandHandler.onUnshutterRequested(unshutterCommand, commandId);

        verify(applicationManagementStateRegistry).setApplicationManagementState(UNSHUTTERING_IN_PROGRESS);
        verify(unshutteringRequestedEventFirer).fire(unshutteringEventCaptor.capture());

        final UnshutteringRequestedEvent unshutteringRequestedEvent = unshutteringEventCaptor.getValue();

        assertThat(unshutteringRequestedEvent.getTarget(), is(unshutterCommand));
        assertThat(unshutteringRequestedEvent.getUnshutteringRequestedAt(), is(now));
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsContextShuttered() throws Exception {

        final UUID commandId = randomUUID();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(SHUTTERED);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'SHUTTERED'");
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsShutteringStarted() throws Exception {

        final UUID commandId = randomUUID();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(SHUTTERING_IN_PROGRESS);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'SHUTTERING_IN_PROGRESS'");
    }

    @Test
    public void shouldIgnoreShutteringRequestIfTheShutteredStateIsUnshutteringStarted() throws Exception {

        final UUID commandId = randomUUID();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(UNSHUTTERING_IN_PROGRESS);

        shutteringSystemCommandHandler.onShutterRequested(new ShutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'SHUTTER'. Context shuttered state is 'UNSHUTTERING_IN_PROGRESS'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsContextUnshuttered() throws Exception {

        final UUID commandId = randomUUID();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(UNSHUTTERED);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'UNSHUTTERED'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsShutteringStarted() throws Exception {

        final UUID commandId = randomUUID();
        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(SHUTTERING_IN_PROGRESS);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'SHUTTERING_IN_PROGRESS'");
    }

    @Test
    public void shouldIgnoreUnshutteringRequestIfTheShutteredStateIsUnshutteringStarted() throws Exception {

        final UUID commandId = randomUUID();

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(UNSHUTTERING_IN_PROGRESS);

        shutteringSystemCommandHandler.onUnshutterRequested(new UnshutterCommand(), commandId);

        verifyZeroInteractions(shutteringRequestedEventFirer);
        verify(logger).info("Ignoring command 'UNSHUTTER'. Context shuttered state is 'UNSHUTTERING_IN_PROGRESS'");
    }
}
