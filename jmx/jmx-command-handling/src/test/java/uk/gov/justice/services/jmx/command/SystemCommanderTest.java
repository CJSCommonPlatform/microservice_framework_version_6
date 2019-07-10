package uk.gov.justice.services.jmx.command;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
    private SystemCommandStore systemCommandStore;

    @Mock
    private SystemCommandScanner systemCommandScanner;

    @InjectMocks
    private SystemCommander systemCommander;

    @Test
    public void shouldFindTheCorrectProxyForTheCommandAndInvoke() throws Exception {

        final TestCommand testCommand = new TestCommand();

        final SystemCommandHandlerProxy systemCommandHandlerProxy = mock(SystemCommandHandlerProxy.class);

        when(systemCommandStore.findCommandProxy(testCommand)).thenReturn(systemCommandHandlerProxy);

        systemCommander.call(testCommand);

        final InOrder inOrder = inOrder(logger, systemCommandHandlerProxy);

        inOrder.verify(logger).info("Received System Command 'TEST_COMMAND'");
        inOrder.verify(systemCommandHandlerProxy).invokeCommand(testCommand);
    }

    @Test
    public void shouldHandleExceptionsThrownBySystemCommandHandlerProxy() throws Exception {

        final SystemCommandInvocationException systemCommandInvocationException = new SystemCommandInvocationException(
                "Ooops",
                new Exception());

        final TestCommand testCommand = new TestCommand();

        final SystemCommandHandlerProxy systemCommandHandlerProxy = mock(SystemCommandHandlerProxy.class);

        when(systemCommandStore.findCommandProxy(testCommand)).thenReturn(systemCommandHandlerProxy);
        doThrow(systemCommandInvocationException).when(systemCommandHandlerProxy).invokeCommand(testCommand);

        try {
            systemCommander.call(testCommand);
            fail();
        } catch (final SystemCommandException expected) {
           assertThat(expected.getCause(), is(systemCommandInvocationException));
           assertThat(expected.getMessage(), is("Failed to run System Command 'TEST_COMMAND'"));

           verify(logger).error("Failed to run System Command 'TEST_COMMAND'", systemCommandInvocationException);
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
}
