package uk.gov.justice.services.management.shuttering.process;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

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
public class ShutteringProcessRunnerTest {

    @Mock
    private ShutteringExecutorsRunner shutteringExecutorsRunner;

    @Mock
    private ShutteringResultsMapper shutteringResultsMapper;

    @Mock
    private ShutteringPostProcess shutteringPostProcess;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringProcessRunner shutteringProcessRunner;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldRunAllShutteringExecutorsAndFireTheSuccessEvent() throws Exception {

        final String commandName = "SHUTTER";

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final ZonedDateTime stateChangedAt = new UtcClock().now();

        when(systemCommand.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(stateChangedAt);

        final List<ShutteringResult> results = singletonList(mock(ShutteringResult.class));
        final List<ShutteringResult> successfulResults = singletonList(mock(ShutteringResult.class));
        final List<ShutteringResult> failureResults = emptyList();

        when(shutteringExecutorsRunner.findAndRunShutteringExecutors(
                commandId,
                systemCommand
        )).thenReturn(results);

        when(shutteringResultsMapper.getFailedResults(results)).thenReturn(failureResults);
        when(shutteringResultsMapper.getSuccessfulResults(results)).thenReturn(successfulResults);

        shutteringProcessRunner.runShuttering(commandId, systemCommand);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                shutteringPostProcess);

        inOrder.verify(logger).info("Running SHUTTER");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("SHUTTER ran with 1 success(es) and 0 error(s)");
        inOrder.verify(shutteringPostProcess).completeShutteringSuccessfully(successfulResults, commandId, systemCommand);

    }

    @Test
    public void shouldFireTheFailureEventIfAnyOfTheSutteringExectorsFail() throws Exception {

        final String commandName = "SHUTTER";

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        final ZonedDateTime stateChangedAt = new UtcClock().now();

        when(systemCommand.getName()).thenReturn(commandName);
        when(clock.now()).thenReturn(stateChangedAt);

        final List<ShutteringResult> results = singletonList(mock(ShutteringResult.class));
        final List<ShutteringResult> successfulResults = singletonList(mock(ShutteringResult.class));
        final List<ShutteringResult> failureResults = singletonList(mock(ShutteringResult.class));

        when(shutteringExecutorsRunner.findAndRunShutteringExecutors(
                commandId,
                systemCommand
        )).thenReturn(results);

        when(shutteringResultsMapper.getFailedResults(results)).thenReturn(failureResults);
        when(shutteringResultsMapper.getSuccessfulResults(results)).thenReturn(successfulResults);

        shutteringProcessRunner.runShuttering(commandId, systemCommand);

        final InOrder inOrder = inOrder(
                logger,
                systemCommandStateChangedEventFirer,
                shutteringPostProcess);

        inOrder.verify(logger).info("Running SHUTTER");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("SHUTTER ran with 1 success(es) and 1 error(s)");
        inOrder.verify(shutteringPostProcess).completeShutteringWithFailures(failureResults, commandId, systemCommand);
    }
}
