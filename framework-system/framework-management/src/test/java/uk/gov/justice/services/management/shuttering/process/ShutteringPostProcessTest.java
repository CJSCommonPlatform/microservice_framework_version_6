package uk.gov.justice.services.management.shuttering.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;
import uk.gov.justice.services.management.shuttering.commands.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.commands.ShutterCommand;
import uk.gov.justice.services.management.shuttering.commands.UnshutterCommand;

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
public class ShutteringPostProcessTest {

    @Mock
    private ShutteringResultsMapper shutteringResultsMapper;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringPostProcess shutteringPostProcess;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldFireCompletedEventIfShutteringWasSuccessful() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime stateChangedAt = new UtcClock().now();
        final ApplicationShutteringCommand applicationShutteringCommand = new UnshutterCommand();
        final List<ShutteringResult> successfulResults = asList(mock(ShutteringResult.class), mock(ShutteringResult.class));
        final List<String> shutteringExecutorNames = asList("ShutteringExecutor_1", "ShutteringExecutor_2");

        when(shutteringResultsMapper.getShutteringExecutorNames(successfulResults)).thenReturn(shutteringExecutorNames);
        when(clock.now()).thenReturn(stateChangedAt);

        shutteringPostProcess.completeShutteringSuccessfully(successfulResults, commandId, applicationShutteringCommand);

        verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final SystemCommandStateChangedEvent stateChangedEvent = systemCommandStateChangedEventCaptor.getValue();

        assertThat(stateChangedEvent.getCommandId(), is(commandId));
        assertThat(stateChangedEvent.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(stateChangedEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(stateChangedEvent.getStatusChangedAt(), is(stateChangedAt));
        assertThat(stateChangedEvent.getMessage(), is("UNSHUTTER completed successfully for [ShutteringExecutor_1, ShutteringExecutor_2]"));
    }

    @Test
    public void shouldFireFailedEventIfShutteringFailed() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime stateChangedAt = new UtcClock().now();
        final ApplicationShutteringCommand applicationShutteringCommand = new ShutterCommand();
        final ShutteringResult failureResult_1 = mock(ShutteringResult.class);
        final ShutteringResult failureResult_2 = mock(ShutteringResult.class);
        final List<ShutteringResult> unsuccessfulResults = asList(failureResult_1, failureResult_2);
        final List<String> shutteringExecutorNames = asList("ShutteringExecutor_1", "ShutteringExecutor_2");

        when(failureResult_1.getMessage()).thenReturn("failure 1");
        when(failureResult_2.getMessage()).thenReturn("failure 2");
        when(shutteringResultsMapper.getShutteringExecutorNames(unsuccessfulResults)).thenReturn(shutteringExecutorNames);
        when(clock.now()).thenReturn(stateChangedAt);

        shutteringPostProcess.completeShutteringWithFailures(unsuccessfulResults, commandId, applicationShutteringCommand);

        final InOrder inOrder = inOrder(logger, systemCommandStateChangedEventFirer);

        inOrder.verify(logger).error("SHUTTER failed with the following 2 errors:");
        inOrder.verify(logger).error("SHUTTER Error: failure 1");
        inOrder.verify(logger).error("SHUTTER Error: failure 2");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final SystemCommandStateChangedEvent stateChangedEvent = systemCommandStateChangedEventCaptor.getValue();

        assertThat(stateChangedEvent.getCommandId(), is(commandId));
        assertThat(stateChangedEvent.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(stateChangedEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(stateChangedEvent.getStatusChangedAt(), is(stateChangedAt));
        assertThat(stateChangedEvent.getMessage(), is("SHUTTER failed. The following ShutteringExecutors failed: [ShutteringExecutor_1, ShutteringExecutor_2]"));
    }
}
