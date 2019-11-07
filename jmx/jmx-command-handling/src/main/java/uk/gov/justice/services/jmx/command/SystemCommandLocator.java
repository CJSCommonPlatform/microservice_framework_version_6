package uk.gov.justice.services.jmx.command;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.Optional;

import javax.inject.Inject;

public class SystemCommandLocator {

    @Inject
    private SystemCommandScanner systemCommandScanner;

    public Optional<SystemCommand> forName(final String commandName) {

        return systemCommandScanner.findCommands().stream()
                .filter(systemCommand -> systemCommand.getName().equals(commandName))
                .findFirst();
    }
}
