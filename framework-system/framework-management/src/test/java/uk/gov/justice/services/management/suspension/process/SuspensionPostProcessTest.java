package uk.gov.justice.services.management.suspension.process;

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
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

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
public class SuspensionPostProcessTest {

    @Mock
    private SuspensionResultsMapper suspensionResultsMapper;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private SuspensionPostProcess suspensionPostProcess;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldFireCompletedEventIfSuspensionWasSuccessful() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime stateChangedAt = new UtcClock().now();
        final SuspensionCommand suspensionCommand = new SuspendCommand();
        final List<SuspensionResult> successfulResults = asList(mock(SuspensionResult.class), mock(SuspensionResult.class));
        final List<String> suspendableNames = asList("Suspendable_1", "Suspendable_2");

        when(suspensionResultsMapper.getSuspendablesNames(successfulResults)).thenReturn(suspendableNames);
        when(clock.now()).thenReturn(stateChangedAt);

        suspensionPostProcess.completeSuspensionSuccessfully(successfulResults, commandId, suspensionCommand);

        verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final SystemCommandStateChangedEvent stateChangedEvent = systemCommandStateChangedEventCaptor.getValue();

        assertThat(stateChangedEvent.getCommandId(), is(commandId));
        assertThat(stateChangedEvent.getSystemCommand(), is(suspensionCommand));
        assertThat(stateChangedEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(stateChangedEvent.getStatusChangedAt(), is(stateChangedAt));
        assertThat(stateChangedEvent.getMessage(), is("SUSPEND completed successfully for [Suspendable_1, Suspendable_2]"));
    }

    @Test
    public void shouldFireFailedEventIfSuspensionFailed() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime stateChangedAt = new UtcClock().now();
        final SuspensionCommand suspensionCommand = new SuspendCommand();
        final SuspensionResult failureResult_1 = mock(SuspensionResult.class);
        final SuspensionResult failureResult_2 = mock(SuspensionResult.class);
        final List<SuspensionResult> unsuccessfulResults = asList(failureResult_1, failureResult_2);
        final List<String> suspendableNames = asList("Suspendable_1", "Suspendable_2");

        when(failureResult_1.getMessage()).thenReturn("failure 1");
        when(failureResult_2.getMessage()).thenReturn("failure 2");
        when(suspensionResultsMapper.getSuspendablesNames(unsuccessfulResults)).thenReturn(suspendableNames);
        when(clock.now()).thenReturn(stateChangedAt);

        suspensionPostProcess.completeSuspensionWithFailures(unsuccessfulResults, commandId, suspensionCommand);

        final InOrder inOrder = inOrder(logger, systemCommandStateChangedEventFirer);

        inOrder.verify(logger).error("SUSPEND failed with the following 2 errors:");
        inOrder.verify(logger).error("SUSPEND Error: failure 1");
        inOrder.verify(logger).error("SUSPEND Error: failure 2");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final SystemCommandStateChangedEvent stateChangedEvent = systemCommandStateChangedEventCaptor.getValue();

        assertThat(stateChangedEvent.getCommandId(), is(commandId));
        assertThat(stateChangedEvent.getSystemCommand(), is(suspensionCommand));
        assertThat(stateChangedEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(stateChangedEvent.getStatusChangedAt(), is(stateChangedAt));
        assertThat(stateChangedEvent.getMessage(), is("SUSPEND failed. The following Suspendables failed: [Suspendable_1, Suspendable_2]"));
    }
}
