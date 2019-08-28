package uk.gov.justice.services.jmx.bootstrap.blacklist;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.Set;

public class BlacklistedCommandsFilter {

    public boolean isSystemCommandAllowed(final String commandName, final Set<SystemCommand> blacklistedCommands) {

        return blacklistedCommands.stream()
                .noneMatch(systemCommand -> systemCommand.getName().equals(commandName));
    }
}
