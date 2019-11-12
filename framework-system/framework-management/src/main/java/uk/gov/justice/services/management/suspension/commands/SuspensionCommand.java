package uk.gov.justice.services.management.suspension.commands;

import static uk.gov.justice.services.management.suspension.commands.SuspendCommand.SUSPEND;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

public interface SuspensionCommand extends SystemCommand {

    default boolean isSuspendCommand() {
        return SUSPEND.equals(getName());
    }
}
