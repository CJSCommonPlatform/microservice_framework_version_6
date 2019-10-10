package uk.gov.justice.services.management.shuttering.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;

import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringSystemCommandHandlerTest {

    @Mock
    private RunShutteringBean runShutteringBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringSystemCommandHandler shutteringSystemCommandHandler;

    @Test
    public void shouldRunShuttering() throws Exception {

        final UUID commandId = randomUUID();
        final ShutterCommand shutterCommand = new ShutterCommand();

        final InOrder inOrder = inOrder(logger, runShutteringBean);

        shutteringSystemCommandHandler.onShutterRequested(shutterCommand, commandId);

        inOrder.verify(logger).info("Received SHUTTER application shuttering command");
        inOrder.verify(runShutteringBean).runShuttering(commandId, shutterCommand);
    }

    @Test
    public void shouldRunUnshuttering() throws Exception {

        final UUID commandId = randomUUID();
        final UnshutterCommand shutterCommand = new UnshutterCommand();

        final InOrder inOrder = inOrder(logger, runShutteringBean);

        shutteringSystemCommandHandler.onUnshutterRequested(shutterCommand, commandId);

        inOrder.verify(logger).info("Received UNSHUTTER application shuttering command");
        inOrder.verify(runShutteringBean).runShuttering(commandId, shutterCommand);
    }
}
