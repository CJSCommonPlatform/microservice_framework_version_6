package uk.gov.justice.services.management.shuttering.process;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.management.shuttering.commands.ShutterCommand;
import uk.gov.justice.services.management.shuttering.commands.UnshutterCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringExecutorsRunnerTest {

    @Mock
    private ShutterRunner shutterRunner;

    @Mock
    private UnshutterRunner unshutterRunner;

    @InjectMocks
    private ShutteringExecutorsRunner shutteringExecutorsRunner;

    @Test
    public void shouldRunShutteringIfCommandIsShutterCommand() throws Exception {

        final UUID commandId = randomUUID();
        final ShutterCommand shutterCommand = new ShutterCommand();

        shutteringExecutorsRunner.findAndRunShutteringExecutors(commandId, shutterCommand);

        verify(shutterRunner).runShuttering(commandId, shutterCommand);
    }

    @Test
    public void shouldRunUnshutteringIfCommandIsUnshutterCommand() throws Exception {

        final UUID commandId = randomUUID();
        final UnshutterCommand unshutterCommand = new UnshutterCommand();

        shutteringExecutorsRunner.findAndRunShutteringExecutors(commandId, unshutterCommand);

        verify(unshutterRunner).runUnshuttering(commandId, unshutterCommand);
    }
}
