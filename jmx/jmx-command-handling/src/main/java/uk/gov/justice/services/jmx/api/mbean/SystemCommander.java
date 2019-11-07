package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.jmx.api.CommandNotFoundException;
import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.command.CommandConverter;
import uk.gov.justice.services.jmx.command.SystemCommandLocator;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private SystemCommandLocator systemCommandLocator;

    @Inject
    private AsynchronousCommandRunner asynchronousCommandRunner;

    @Inject
    private SystemCommandScanner systemCommandScanner;

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Inject
    private CommandConverter commandConverter;

    @Inject
    private Logger logger;

    @Override
    public UUID call(final String systemCommandName) {

        logger.info(format("Received System Command '%s'", systemCommandName));

        final SystemCommand systemCommand = systemCommandLocator
                .forName(systemCommandName)
                .orElseThrow(() -> new UnrunnableSystemCommandException(format("The system command '%s' is not supported on this context.", systemCommandName)));

        if (systemCommandStateBean.commandInProgress(systemCommand)) {
            throw new UnrunnableSystemCommandException(format("Cannot run system command '%s'. A previous call to that command is still in progress.", systemCommand.getName()));
        }

        return asynchronousCommandRunner.run(systemCommand);
    }

    @Override
    public List<SystemCommandDetails> listCommands() {

        return systemCommandScanner.findCommands().stream()
                .map(commandConverter::toCommandDetails)
                .collect(toList());
    }

    @Override
    public SystemCommandStatus getCommandStatus(final UUID commandId) {

        return systemCommandStateBean
                .getCommandStatus(commandId)
                .orElseThrow(() -> new CommandNotFoundException(format("No SystemCommand found with id %s", commandId)));
    }
}
