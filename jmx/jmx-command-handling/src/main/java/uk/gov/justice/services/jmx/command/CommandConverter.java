package uk.gov.justice.services.jmx.command;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;

public class CommandConverter {

    public SystemCommandDetails toCommandDetails(final SystemCommand systemCommand) {

        return new SystemCommandDetails(
                systemCommand.getName(),
                systemCommand.getDescription()
        );
    }
}
