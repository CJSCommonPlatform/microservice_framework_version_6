package uk.gov.justice.services.management.suspension.process;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SuspendablesRunnerTest {

    @Mock
    private SuspensionRunner suspensionRunner;

    @Mock
    private UnsuspensionRunner unsuspensionRunner;

    @InjectMocks
    private SuspendablesRunner suspendablesRunner;

    @Test
    public void shouldRunSuspensionIfCommandIsSuspendCommand() throws Exception {

        final UUID commandId = randomUUID();
        final SuspendCommand suspendCommand = new SuspendCommand();

        suspendablesRunner.findAndRunSuspendables(commandId, suspendCommand);

        verify(suspensionRunner).runSuspension(commandId, suspendCommand);
    }

    @Test
    public void shouldRunUnsuspensionIfCommandIsUnsuspendCommand() throws Exception {

        final UUID commandId = randomUUID();
        final UnsuspendCommand unsuspendCommand = new UnsuspendCommand();

        suspendablesRunner.findAndRunSuspendables(commandId, unsuspendCommand);

        verify(unsuspensionRunner).runUsuspension(commandId, unsuspendCommand);
    }
}
