package uk.gov.justice.services.management.suspension.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SuspensionRunnerTest {

    @Mock
    private SuspendablesProvider suspendablesProvider;

    @Mock
    private SuspensionFailedHandler suspensionFailedHandler;

    @Mock
    private Logger logger;

    @InjectMocks
    private SuspensionRunner suspensionRunner;

    @Test
    public void shouldRunSuspensionOnSuspenderIfTheItSupportsIt() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = new SuspendCommand();

        final Suspendable suspendable_1 = mock(Suspendable.class);
        final Suspendable suspendable_2 = mock(Suspendable.class);
        final Suspendable suspendable_3 = mock(Suspendable.class);
        final Suspendable suspendable_4 = mock(Suspendable.class);

        final SuspensionResult suspensionResult_1 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_3 = mock(SuspensionResult.class);

        when(suspendablesProvider.getSuspendables()).thenReturn(asList(
                suspendable_1,
                suspendable_2,
                suspendable_3,
                suspendable_4
        ));

        when(suspendable_1.shouldSuspend()).thenReturn(true);
        when(suspendable_3.shouldSuspend()).thenReturn(true);

        when(suspendable_1.getName()).thenReturn("Suspendable 1");
        when(suspendable_3.getName()).thenReturn("Suspendable 3");

        when(suspendable_1.suspend(commandId, suspensionCommand)).thenReturn(suspensionResult_1);
        when(suspendable_3.suspend(commandId, suspensionCommand)).thenReturn(suspensionResult_3);

        final List<SuspensionResult> suspensionResults = suspensionRunner.runSuspension(commandId, suspensionCommand);

        assertThat(suspensionResults.size(), is(2));
        assertThat(suspensionResults, hasItem(suspensionResult_1));
        assertThat(suspensionResults, hasItem(suspensionResult_3));

        verify(logger).info("Suspending 'Suspendable 1'");
        verify(logger).info("Suspending 'Suspendable 3'");

        verify(suspendable_2, never()).suspend(commandId, suspensionCommand);
        verify(suspendable_4, never()).suspend(commandId, suspensionCommand);
    }

    @Test
    public void shouldHandleUnsuspensionExceptions() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Oooops");

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = new SuspendCommand();

        final Suspendable suspendable_1 = mock(Suspendable.class);
        final Suspendable suspendable_2 = mock(Suspendable.class);
        final Suspendable suspendable_3 = mock(Suspendable.class);
        final Suspendable suspendable_4 = mock(Suspendable.class);

        final SuspensionResult failureResult = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_3 = mock(SuspensionResult.class);

        when(suspendablesProvider.getSuspendables()).thenReturn(asList(
                suspendable_1,
                suspendable_2,
                suspendable_3,
                suspendable_4
        ));

        when(suspendable_1.shouldSuspend()).thenReturn(true);
        when(suspendable_3.shouldSuspend()).thenReturn(true);

        when(suspendable_1.getName()).thenReturn("Executor 1");
        when(suspendable_3.getName()).thenReturn("Executor 3");

        when(suspendable_1.suspend(commandId, suspensionCommand)).thenThrow(nullPointerException);
        when(suspendable_3.suspend(commandId, suspensionCommand)).thenReturn(suspensionResult_3);

        when(suspensionFailedHandler.onSuspensionFailed(
                commandId,
                suspensionCommand,
                suspendable_1,
                nullPointerException
        )).thenReturn(failureResult);

        final List<SuspensionResult> suspensionResults = suspensionRunner.runSuspension(commandId, suspensionCommand);

        assertThat(suspensionResults.size(), is(2));
        assertThat(suspensionResults, hasItem(failureResult));
        assertThat(suspensionResults, hasItem(suspensionResult_3));
    }
}
