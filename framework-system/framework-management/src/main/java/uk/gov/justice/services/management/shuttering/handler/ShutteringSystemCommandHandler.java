package uk.gov.justice.services.management.shuttering.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.management.shuttering.commands.ShutterCommand.SHUTTER;
import static uk.gov.justice.services.management.shuttering.commands.UnshutterCommand.UNSHUTTER;

import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.commands.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.commands.ShutterCommand;
import uk.gov.justice.services.management.shuttering.commands.UnshutterCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutteringSystemCommandHandler {

    @Inject
    private RunShutteringBean runShutteringBean;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(SHUTTER)
    public void onShutterRequested(final ShutterCommand shutterCommand, final UUID commandId) {
        doRun(shutterCommand, commandId);
    }

    @HandlesSystemCommand(UNSHUTTER)
    public void onUnshutterRequested(final UnshutterCommand unshutterCommand, final UUID commandId) {
        doRun(unshutterCommand, commandId);
    }

    private void doRun(final ApplicationShutteringCommand applicationShutteringCommand, final UUID commandId) {
        logger.info(format("Received %s application shuttering command", applicationShutteringCommand.getName()));
        runShutteringBean.runShuttering(commandId, applicationShutteringCommand);
    }
}
