package uk.gov.justice.services.management.shuttering.process;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.jmx.api.command.ShutterCommand.SHUTTER;
import static uk.gov.justice.services.jmx.api.command.UnshutterCommand.UNSHUTTER;

import uk.gov.justice.services.jmx.api.SystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutteringExecutorsRunner {

    @Inject
    private ShutterRunner shutterRunner;

    @Inject
    private UnshutterRunner unshutterRunner;

    
    public List<ShutteringResult> findAndRunShutteringExecutors(final UUID commandId, final SystemCommand systemCommand) {

        final String commandName = systemCommand.getName();
        if (SHUTTER.equals(commandName)) {
            return shutterRunner.runShuttering(commandId, systemCommand);
        }

        if (UNSHUTTER.equals(commandName)) {
            return unshutterRunner.runUnshuttering(commandId, systemCommand);
        }

        throw new SystemCommandException(format("Failed to run shutter command. Command %s is not supported", commandName));
    }
}
