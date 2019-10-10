package uk.gov.justice.services.jmx.api.command;

import static uk.gov.justice.services.jmx.api.command.EventCatchupCommand.CATCHUP;

public interface CatchupCommand extends SystemCommand {

    default boolean isEventCatchup() {
        return CATCHUP.equals(getName());
    }
}
