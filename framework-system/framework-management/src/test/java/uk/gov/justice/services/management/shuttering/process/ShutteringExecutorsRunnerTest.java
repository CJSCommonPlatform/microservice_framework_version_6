package uk.gov.justice.services.management.shuttering.process;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.jmx.api.SystemCommandException;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;

import java.util.UUID;

import javax.inject.Inject;

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
        final SystemCommand systemCommand = new ShutterCommand();

        shutteringExecutorsRunner.findAndRunShutteringExecutors(commandId, systemCommand);

        verify(shutterRunner).runShuttering(commandId, systemCommand);
    }

    @Test
    public void shouldRunUnshutteringIfCommandIsUnshutterCommand() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = new UnshutterCommand();

        shutteringExecutorsRunner.findAndRunShutteringExecutors(commandId, systemCommand);

        verify(unshutterRunner).runUnshuttering(commandId, systemCommand);
    }

    @Test
    public void shouldThrowExceptionIfCommandIsNeitherShutteringOrUnshuttering() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(systemCommand.getName()).thenReturn("RANDOM_COMMAND");

        try {
            shutteringExecutorsRunner.findAndRunShutteringExecutors(commandId, systemCommand);
            fail();
        } catch (final SystemCommandException expected) {
            assertThat(expected.getMessage(), is("Failed to run shutter command. Command RANDOM_COMMAND is not supported"));
        }
    }
}
