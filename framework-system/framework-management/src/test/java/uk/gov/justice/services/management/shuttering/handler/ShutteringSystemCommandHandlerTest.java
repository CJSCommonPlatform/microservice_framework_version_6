package uk.gov.justice.services.management.shuttering.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ShutterSystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringSystemCommandHandlerTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Mock
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

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

        when(clock.now()).thenReturn(now);

        shutteringSystemCommandHandler.onUnshutterRequested(unshutterSystemCommand);

        verify(unshutteringRequestedEventFirer).fire(unshutteringEventCaptor.capture());

        final UnshutteringRequestedEvent unshutteringRequestedEvent = unshutteringEventCaptor.getValue();

        assertThat(unshutteringRequestedEvent.getTarget(), is(unshutterSystemCommand));
        assertThat(unshutteringRequestedEvent.getUnshutteringRequestedAt(), is(now));
    }
}
