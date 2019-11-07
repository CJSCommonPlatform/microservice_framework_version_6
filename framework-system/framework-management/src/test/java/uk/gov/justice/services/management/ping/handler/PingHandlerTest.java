package uk.gov.justice.services.management.ping.handler;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.ping.commands.PingCommand;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PingHandlerTest {

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @InjectMocks
    private PingHandler pingHandler;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldLogAPingMessage() throws Exception {

        final UUID commandId = randomUUID();
        final PingCommand pingCommand = new PingCommand();

        final ZonedDateTime startedAt = of(2019, 11, 7, 15, 22, 25, 0, UTC);
        final ZonedDateTime completeAt = startedAt.plusSeconds(2);

        when(clock.now()).thenReturn(startedAt, completeAt);

        pingHandler.ping(pingCommand, commandId);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, logger);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("********** Received system command 'PING' at 2019-11-07T15:22:25Z **********");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(pingCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startedAt));
        assertThat(startEvent.getMessage(), is("Ping command received"));

        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completeEvent.getSystemCommand(), is(pingCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(completeAt));
        assertThat(completeEvent.getMessage(), is("Ping command complete"));
    }
}
