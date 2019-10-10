package uk.gov.justice.services.jmx.api.command;

import static uk.gov.justice.services.jmx.api.command.UnshutterCommand.UNSHUTTER;

public interface ApplicationShutteringCommand extends SystemCommand {

    default boolean isUnshuttering() {
        return UNSHUTTER.equals(getName());
    }
}
