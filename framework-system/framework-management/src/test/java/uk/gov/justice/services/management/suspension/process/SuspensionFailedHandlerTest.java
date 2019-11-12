package uk.gov.justice.services.management.suspension.process;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SuspensionFailedHandlerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private SuspensionFailedHandler suspensionFailedHandler;

    @Test
    public void shouldLogErrorAndReturnAFailureResult() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final String suspendableName = "SuspendableName";

        final SuspensionCommand suspensionCommand = new UnsuspendCommand();
        final Suspendable suspendable = mock(Suspendable.class);

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        when(suspendable.getName()).thenReturn(suspendableName);

        final SuspensionResult suspensionResult = suspensionFailedHandler.onSuspensionFailed(
                commandId,
                suspensionCommand,
                suspendable,
                nullPointerException
        );

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(suspensionResult.getMessage(), is("UNSUSPEND failed for SuspendableName. java.lang.NullPointerException: Ooops"));
        assertThat(suspensionResult.getSuspendableName(), is(suspendableName));
        assertThat(suspensionResult.getSystemCommand(), is(suspensionCommand));
        assertThat(suspensionResult.getException(), is(of(nullPointerException)));

        verify(logger).error("UNSUSPEND failed for SuspendableName. java.lang.NullPointerException: Ooops", nullPointerException);
    }
}
