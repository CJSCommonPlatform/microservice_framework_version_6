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
public class UnsuspensionRunnerTest {

    @Mock
    private SuspendablesProvider suspendablesProvider;

    @Mock
    private SuspensionFailedHandler suspensionFailedHandler;

    @Mock
    private Logger logger;

    @InjectMocks
    private UnsuspensionRunner unsuspensionRunner;

    @Test
    public void shouldRunUnshutteringOnShutteringExecutorIfTheExecutorSupportsIt() throws Exception {

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

        when(suspendable_1.shouldUnsuspend()).thenReturn(true);
        when(suspendable_3.shouldUnsuspend()).thenReturn(true);

        when(suspendable_1.getName()).thenReturn("Executor 1");
        when(suspendable_3.getName()).thenReturn("Executor 3");

        when(suspendable_1.unsuspend(commandId, suspensionCommand)).thenReturn(suspensionResult_1);
        when(suspendable_3.unsuspend(commandId, suspensionCommand)).thenReturn(suspensionResult_3);


        final List<SuspensionResult> suspensionResults = unsuspensionRunner.runUsuspension(commandId, suspensionCommand);

        assertThat(suspensionResults.size(), is(2));
        assertThat(suspensionResults, hasItem(suspensionResult_1));
        assertThat(suspensionResults, hasItem(suspensionResult_3));

        verify(logger).info("Unshuttering Executor 1");
        verify(logger).info("Unshuttering Executor 3");
        verify(suspendable_2, never()).unsuspend(commandId, suspensionCommand);
        verify(suspendable_4, never()).unsuspend(commandId, suspensionCommand);
    }

    @Test
    public void shouldHandleShutteringExceptions() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

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

        when(suspendable_1.shouldUnsuspend()).thenReturn(true);
        when(suspendable_3.shouldUnsuspend()).thenReturn(true);

        when(suspendable_1.getName()).thenReturn("Executor 1");
        when(suspendable_3.getName()).thenReturn("Executor 3");

        when(suspendable_1.unsuspend(commandId, suspensionCommand)).thenThrow(nullPointerException);
        when(suspendable_3.unsuspend(commandId, suspensionCommand)).thenReturn(suspensionResult_3);

        when(suspensionFailedHandler.onSuspensionFailed(
                commandId,
                suspensionCommand,
                suspendable_1,
                nullPointerException)
        ).thenReturn(failureResult);

        final List<SuspensionResult> suspensionResults = unsuspensionRunner.runUsuspension(commandId, suspensionCommand);

        assertThat(suspensionResults.size(), is(2));
        assertThat(suspensionResults, hasItem(failureResult));
        assertThat(suspensionResults, hasItem(suspensionResult_3));
    }
}
