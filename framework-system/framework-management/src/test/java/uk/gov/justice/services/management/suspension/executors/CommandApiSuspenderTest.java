package uk.gov.justice.services.management.suspension.executors;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;

import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CommandApiSuspenderTest {

    @Mock
    private CommandApiSuspensionBean commandApiSuspensionBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private CommandApiSuspender commandApiSuspender;

    @Test
    public void shouldSuspendCommandApi() throws Exception {

        assertThat(commandApiSuspender.shouldSuspend(), is(true));
    }

    @Test
    public void shouldUnsuspendCommandApi() throws Exception {

        assertThat(commandApiSuspender.shouldUnsuspend(), is(true));
    }

    @Test
    public void shouldCallTheCommandApiSuspensionBeanAndShutter() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        final SuspensionResult suspensionResult = commandApiSuspender.suspend(commandId, suspensionCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getSystemCommand(), is(suspensionCommand));
        assertThat(suspensionResult.getMessage(), is("Command API suspended with no errors"));
        assertThat(suspensionResult.getSuspendableName(), is("CommandApiSuspender"));
        assertThat(suspensionResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(logger, commandApiSuspensionBean);

        inOrder.verify(logger).info("Suspending Command API");
        inOrder.verify(commandApiSuspensionBean).suspend();
        inOrder.verify(logger).info("Suspension of Command API complete");
    }

    @Test
    public void shouldCallCommandApiSuspensionBeanAndUnsuspend() throws Exception {

        final UUID commandId = randomUUID();
        final SuspensionCommand suspensionCommand = mock(SuspensionCommand.class);

        final SuspensionResult suspensionResult = commandApiSuspender.unsuspend(commandId, suspensionCommand);

        assertThat(suspensionResult.getCommandId(), is(commandId));
        assertThat(suspensionResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(suspensionResult.getSystemCommand(), is(suspensionCommand));
        assertThat(suspensionResult.getMessage(), is("Command API unsuspended with no errors"));
        assertThat(suspensionResult.getSuspendableName(), is("CommandApiSuspender"));
        assertThat(suspensionResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(logger, commandApiSuspensionBean);

        inOrder.verify(logger).info("Unsuspending Command API");
        inOrder.verify(commandApiSuspensionBean).unsuspend();
        inOrder.verify(logger).info("Unsuspension of Command API complete");
    }
}
