package uk.gov.justice.services.jmx.runner;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_RECEIVED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class AsynchronousCommandRunner {

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private SystemCommandRunner systemCommandRunner;

    @Inject
    private UtcClock clock;


    public UUID run(final SystemCommand systemCommand) {

        final UUID commandId = randomUUID();

        fireCommandReceived(commandId, systemCommand);

        managedExecutorService.submit(new RunSystemCommandTask(
                systemCommandRunner,
                systemCommand,
                commandId
        ));

        return commandId;
    }

    public boolean commandNotSupported(final SystemCommand systemCommand) {
        return ! systemCommandRunner.isSupported(systemCommand);
    }

    private void fireCommandReceived(final UUID commandId, final SystemCommand systemCommand) {
        final String commandName = systemCommand.getName();
        systemCommandStateBean.addSystemCommandState(new SystemCommandStatus(
                commandId,
                commandName,
                COMMAND_RECEIVED,
                clock.now(),
                format("System Command %s Received", commandName))
        );
    }
}
