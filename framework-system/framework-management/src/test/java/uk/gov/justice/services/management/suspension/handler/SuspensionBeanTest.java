package uk.gov.justice.services.management.suspension.handler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;
import uk.gov.justice.services.management.suspension.process.SuspendablesRunner;
import uk.gov.justice.services.management.suspension.process.SuspensionPostProcess;
import uk.gov.justice.services.management.suspension.process.SuspensionResultsMapper;

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
public class SuspensionBeanTest {

    @Mock
    private SuspendablesRunner suspendablesRunner;

    @Mock
    private SuspensionResultsMapper suspensionResultsMapper;

    @Mock
    private SuspensionPostProcess suspensionPostProcess;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private SuspensionBean suspensionBean;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldRunAllShutteringExecutorsAndFireTheSuccessEvent() throws Exception {

        final String commandName = "SHUTTER";

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        final ZonedDateTime stateChangedAt = new UtcClock().now();

        when(suspensionCommand.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(stateChangedAt);

        final List<SuspensionResult> results = singletonList(mock(SuspensionResult.class));
        final List<SuspensionResult> successfulResults = singletonList(mock(SuspensionResult.class));
        final List<SuspensionResult> failureResults = emptyList();

        when(suspendablesRunner.findAndRunSuspendables(
                commandId,
                suspensionCommand
        )).thenReturn(results);

        when(suspensionResultsMapper.getFailedResults(results)).thenReturn(failureResults);
        when(suspensionResultsMapper.getSuccessfulResults(results)).thenReturn(successfulResults);

        suspensionBean.runSuspension(commandId, suspensionCommand);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                suspensionPostProcess);

        inOrder.verify(logger).info("Running SHUTTER");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("SHUTTER ran with 1 success(es) and 0 error(s)");
        inOrder.verify(suspensionPostProcess).completeSuspensionSuccessfully(successfulResults, commandId, suspensionCommand);
    }

    @Test
    public void shouldFireTheFailureEventIfAnyOfTheSutteringExectorsFail() throws Exception {

        final String commandName = "SHUTTER";

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        final ZonedDateTime stateChangedAt = new UtcClock().now();

        when(suspensionCommand.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(stateChangedAt);

        final List<SuspensionResult> results = singletonList(mock(SuspensionResult.class));
        final List<SuspensionResult> successfulResults = singletonList(mock(SuspensionResult.class));
        final List<SuspensionResult> failureResults = singletonList(mock(SuspensionResult.class));

        when(suspendablesRunner.findAndRunSuspendables(
                commandId,
                suspensionCommand
        )).thenReturn(results);

        when(suspensionResultsMapper.getFailedResults(results)).thenReturn(failureResults);
        when(suspensionResultsMapper.getSuccessfulResults(results)).thenReturn(successfulResults);

        suspensionBean.runSuspension(commandId, suspensionCommand);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                suspensionPostProcess);

        inOrder.verify(logger).info("Running SHUTTER");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("SHUTTER ran with 1 success(es) and 1 error(s)");
        inOrder.verify(suspensionPostProcess).completeSuspensionWithFailures(failureResults, commandId, suspensionCommand);
    }
}
