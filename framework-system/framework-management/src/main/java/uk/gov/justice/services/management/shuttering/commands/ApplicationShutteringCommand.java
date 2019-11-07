package uk.gov.justice.services.management.shuttering.commands;

import static uk.gov.justice.services.management.shuttering.commands.UnshutterCommand.UNSHUTTER;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

public interface ApplicationShutteringCommand extends SystemCommand {

    default boolean isUnshuttering() {
        return UNSHUTTER.equals(getName());
    }
}
