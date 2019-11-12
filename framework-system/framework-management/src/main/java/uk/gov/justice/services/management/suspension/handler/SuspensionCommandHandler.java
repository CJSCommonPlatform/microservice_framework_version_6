package uk.gov.justice.services.management.suspension.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.management.suspension.commands.SuspendCommand.SUSPEND;
import static uk.gov.justice.services.management.suspension.commands.UnsuspendCommand.UNSUSPEND;

import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.suspension.commands.SuspendCommand;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;
import uk.gov.justice.services.management.suspension.commands.UnsuspendCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SuspensionCommandHandler {

    @Inject
    private SuspensionBean suspensionBean;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(SUSPEND)
    public void onSuspendRequested(final SuspendCommand suspendCommand, final UUID commandId) {
        doRun(suspendCommand, commandId);
    }

    @HandlesSystemCommand(UNSUSPEND)
    public void onUnsuspendRequested(final UnsuspendCommand unsuspendCommand, final UUID commandId) {
        doRun(unsuspendCommand, commandId);
    }

    private void doRun(final SuspensionCommand suspensionCommand, final UUID commandId) {
        logger.info(format("Received %s application command", suspensionCommand.getName()));
        suspensionBean.runSuspension(commandId, suspensionCommand);
    }
}
