package uk.gov.justice.services.jmx.command;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class HandlesSystemCommanderTest {

    @Mock
    private Logger logger;

    @Mock
    private SystemCommandStore systemCommandStore;

    @InjectMocks
    private SystemCommander systemCommander;

    @Test
    public void shouldFindTheCorrectProxyForTheCommandAndInvoke() throws Exception {

        final String command = "some command";

        final SystemCommand systemCommand = mock(SystemCommand.class);
        final SystemCommandHandlerProxy systemCommandHandlerProxy = mock(SystemCommandHandlerProxy.class);

        when(systemCommand.getName()).thenReturn(command);
        when(systemCommandStore.findCommandProxy(systemCommand)).thenReturn(systemCommandHandlerProxy);

        systemCommander.runCommand(systemCommand);

        final InOrder inOrder = inOrder(logger, systemCommandHandlerProxy);

        inOrder.verify(logger).info("Received System Command 'some command'");
        inOrder.verify(systemCommandHandlerProxy).invokeCommand();
    }
}
