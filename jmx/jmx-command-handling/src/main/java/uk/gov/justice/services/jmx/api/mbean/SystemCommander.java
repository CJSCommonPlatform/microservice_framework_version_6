package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.CommandNotFoundException;
import uk.gov.justice.services.jmx.api.UnrunnableSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private Logger logger;

    @Inject
    private AsynchronousCommandRunner asynchronousCommandRunnerBean;

    @Inject
    private SystemCommandScanner systemCommandScanner;

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Override
    public UUID call(final SystemCommand systemCommand) {

        logger.info(format("Received System Command '%s'", systemCommand.getName()));

        if(asynchronousCommandRunnerBean.commandNotSupported(systemCommand)) {
           throw new UnrunnableSystemCommandException(format("The system command '%s' is not supported on this context.", systemCommand.getName()));
        } if(systemCommandStateBean.commandInProgress(systemCommand)) {
           throw new UnrunnableSystemCommandException(format("Cannot run system command '%s'. A previous call to that command is still in progress.", systemCommand.getName()));
        }

        return asynchronousCommandRunnerBean.run(systemCommand);
    }

    @Override
    public List<SystemCommand> listCommands() {
       return systemCommandScanner.findCommands();
    }

    @Override
    public SystemCommandStatus getCommandStatus(final UUID commandId) {

        return systemCommandStateBean
                .getCommandStatus(commandId)
                .orElseThrow(() -> new CommandNotFoundException(format("No SystemCommand found with id %s", commandId)));
    }
}
