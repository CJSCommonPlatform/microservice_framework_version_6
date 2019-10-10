package uk.gov.justice.services.management.shuttering.process;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class ShutteringExecutorsRunner {

    @Inject
    private ShutterRunner shutterRunner;

    @Inject
    private UnshutterRunner unshutterRunner;


    public List<ShutteringResult> findAndRunShutteringExecutors(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {

        if (applicationShutteringCommand.isUnshuttering()) {
            return unshutterRunner.runUnshuttering(commandId, applicationShutteringCommand);
        }

        return shutterRunner.runShuttering(commandId, applicationShutteringCommand);
    }
}
