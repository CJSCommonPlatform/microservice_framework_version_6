package uk.gov.justice.services.management.suspension.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.inOrder;

import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SuspensionCommandHandlerTest {

    @Mock
    private SuspensionBean suspensionBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private SuspensionCommandHandler suspensionCommandHandler;

    @Test
    public void shouldRunSuspension() throws Exception {

        final UUID commandId = randomUUID();
        final SuspendCommand suspendCommand = new SuspendCommand();

        final InOrder inOrder = inOrder(logger, suspensionBean);

        suspensionCommandHandler.onSuspendRequested(suspendCommand, commandId);

        inOrder.verify(logger).info("Received SUSPEND application command");
        inOrder.verify(suspensionBean).runSuspension(commandId, suspendCommand);
    }

    @Test
    public void shouldRunUnsuspension() throws Exception {

        final UUID commandId = randomUUID();
        final UnsuspendCommand shutterCommand = new UnsuspendCommand();

        final InOrder inOrder = inOrder(logger, suspensionBean);

        suspensionCommandHandler.onUnsuspendRequested(shutterCommand, commandId);

        inOrder.verify(logger).info("Received UNSUSPEND application command");
        inOrder.verify(suspensionBean).runSuspension(commandId, shutterCommand);
    }
}
