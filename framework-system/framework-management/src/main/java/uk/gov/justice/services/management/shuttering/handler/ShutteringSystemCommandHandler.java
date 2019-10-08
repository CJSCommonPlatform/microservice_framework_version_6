package uk.gov.justice.services.management.shuttering.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.ShutterCommand.SHUTTER;
import static uk.gov.justice.services.jmx.api.command.UnshutterCommand.UNSHUTTER;

import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;

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

    private void doRun(final SystemCommand systemCommand, final UUID commandId) {
        logger.info(format("Received %s system command", systemCommand.getName()));
        runShutteringBean.runShuttering(commandId, systemCommand);
    }
}
