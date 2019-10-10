package uk.gov.justice.services.jmx.command;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.SystemCommandException;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;
import uk.gov.justice.services.jmx.api.command.PingCommand;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSystemCommandStoreTest {

    @Mock
    private Logger logger;
 
    @InjectMocks
    private DefaultSystemCommandStore defaultSystemCommandStore;

    @Test
    public void shouldKnowWhichCommandsAreSupported() throws Exception {

        final String commandName = "COMMAND";

        final SystemCommandHandlerProxy systemCommandHandlerProxy_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_3 = mock(SystemCommandHandlerProxy.class);

        final SystemCommand rebuildCommand = new RebuildCommand();
        final SystemCommand pingCommand = new PingCommand();
        final SystemCommand catchupCommand = new EventCatchupCommand();

        when(systemCommandHandlerProxy_1.getCommandName()).thenReturn(rebuildCommand.getName());
        when(systemCommandHandlerProxy_2.getCommandName()).thenReturn(pingCommand.getName());
        when(systemCommandHandlerProxy_3.getCommandName()).thenReturn(catchupCommand.getName());

        when(systemCommandHandlerProxy_1.getInstance()).thenReturn(new DummyHandler_1());
        when(systemCommandHandlerProxy_2.getInstance()).thenReturn(new DummyHandler_2());
        when(systemCommandHandlerProxy_3.getInstance()).thenReturn(new DummyHandler_3());

        defaultSystemCommandStore.store(asList(systemCommandHandlerProxy_1, systemCommandHandlerProxy_2, systemCommandHandlerProxy_3));

        assertThat(defaultSystemCommandStore.isSupported(rebuildCommand), is(true));
        assertThat(defaultSystemCommandStore.isSupported(pingCommand), is(true));
        assertThat(defaultSystemCommandStore.isSupported(catchupCommand), is(true));

        assertThat(defaultSystemCommandStore.isSupported(new ShutterCommand()), is(false));
    }

    @Test
    public void shouldFindTheCorrectHandlerForTheCommandName() throws Exception {

        final String commandName_1 = "COMMAND_1";
        final String commandName_2 = "COMMAND_2";
        final String commandName_3 = "COMMAND_3";

        final SystemCommandHandlerProxy systemCommandHandlerProxy_1 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_2 = mock(SystemCommandHandlerProxy.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy_3 = mock(SystemCommandHandlerProxy.class);

        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(systemCommandHandlerProxy_1.getCommandName()).thenReturn(commandName_1);
        when(systemCommandHandlerProxy_2.getCommandName()).thenReturn(commandName_2);
        when(systemCommandHandlerProxy_3.getCommandName()).thenReturn(commandName_3);

        when(systemCommandHandlerProxy_1.getInstance()).thenReturn(new DummyHandler_1());
        when(systemCommandHandlerProxy_2.getInstance()).thenReturn(new DummyHandler_2());
        when(systemCommandHandlerProxy_3.getInstance()).thenReturn(new DummyHandler_3());

        when(systemCommand.getName()).thenReturn(commandName_2);

        defaultSystemCommandStore.store(asList(systemCommandHandlerProxy_1, systemCommandHandlerProxy_2, systemCommandHandlerProxy_3));

        verify(logger).info("Registering class DummyHandler_1 as system command handler for 'COMMAND_1'");
        verify(logger).info("Registering class DummyHandler_2 as system command handler for 'COMMAND_2'");
        verify(logger).info("Registering class DummyHandler_3 as system command handler for 'COMMAND_3'");

        final SystemCommandHandlerProxy systemCommandHandlerProxy = defaultSystemCommandStore.findCommandProxy(systemCommand);

        assertThat(systemCommandHandlerProxy, is(systemCommandHandlerProxy_2));
    }

    @Test
    public void shouldFailIfNoHandlerFound() throws Exception {

        final String missinCommandName = "This command does not exist";

        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(systemCommand.getName()).thenReturn(missinCommandName);
        try {
            defaultSystemCommandStore.findCommandProxy(systemCommand);
            fail();
        } catch (final SystemCommandException expected) {
            assertThat(expected.getMessage(), is("Failed to find SystemCommandHandler for command 'This command does not exist'"));
        }
    }

    private class DummyHandler_1 {}
    private class DummyHandler_2 {}
    private class DummyHandler_3 {}
}
