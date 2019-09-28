package uk.gov.justice.services.jmx.api.mbean;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERED;

import uk.gov.justice.services.jmx.api.UnsupportedSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.state.ApplicationManagementState;
import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.command.TestCommand;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SystemCommanderTest {

    @Mock
    private Logger logger;

    @Mock
    private AsynchronousCommandRunner asynchronousCommandRunner;

    @Mock
    private SystemCommandScanner systemCommandScanner;

    @Mock
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    @InjectMocks
    private SystemCommander systemCommander;

    @Test
    public void shouldRunTheSystemCommandIfSuppoorted() throws Exception {

        final UUID commandId = randomUUID();
        final TestCommand testCommand = new TestCommand();

        when(asynchronousCommandRunner.isSupported(testCommand)).thenReturn(true);
        when(asynchronousCommandRunner.run(testCommand)).thenReturn(commandId);

        assertThat(systemCommander.call(testCommand), is(commandId));

        final InOrder inOrder = inOrder(logger, asynchronousCommandRunner);

        inOrder.verify(logger).info("Received System Command 'TEST_COMMAND'");
        inOrder.verify(asynchronousCommandRunner).run(testCommand);
    }

    @Test
    public void shouldFailIfSystemCommandNotSupported() throws Exception {

        final TestCommand testCommand = new TestCommand();

        when(asynchronousCommandRunner.isSupported(testCommand)).thenReturn(false);

        try {
            systemCommander.call(testCommand);
            fail();
        } catch (final UnsupportedSystemCommandException expected) {
            assertThat(expected.getMessage(), is("The system command 'TEST_COMMAND' is not supported on this context."));
        }
    }

    @Test
    public void shouldListAllSystemCommands() throws Exception {

        final SystemCommand systemCommand_1 = mock(SystemCommand.class);
        final SystemCommand systemCommand_2 = mock(SystemCommand.class);
        final SystemCommand systemCommand_3 = mock(SystemCommand.class);

        when(systemCommandScanner.findCommands()).thenReturn(asList(
                systemCommand_1,
                systemCommand_2,
                systemCommand_3));

        final List<SystemCommand> systemCommands = systemCommander.listCommands();

        assertThat(systemCommands.size(), is(3));
        assertThat(systemCommands, hasItem(systemCommand_1));
        assertThat(systemCommands, hasItem(systemCommand_2));
        assertThat(systemCommands, hasItem(systemCommand_3));
    }

    @Test
    public void shouldGetTheApplicationState() throws Exception {

        final ApplicationManagementState applicationManagementState = SHUTTERED;

        when(applicationManagementStateRegistry.getApplicationManagementState()).thenReturn(applicationManagementState);

        assertThat(systemCommander.getApplicationState(), is(applicationManagementState));
    }
}
