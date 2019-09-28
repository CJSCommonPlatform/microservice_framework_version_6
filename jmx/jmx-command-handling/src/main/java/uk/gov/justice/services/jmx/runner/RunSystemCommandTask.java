package uk.gov.justice.services.jmx.runner;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.UUID;
import java.util.concurrent.Callable;

public class RunSystemCommandTask implements Callable<Boolean> {

    private final SystemCommandRunner systemCommandRunner;
    private final SystemCommand systemCommand;
    private final UUID commandId;

    public RunSystemCommandTask(
            final SystemCommandRunner systemCommandRunner,
            final SystemCommand systemCommand,
            final UUID commandId) {
        this.systemCommandRunner = systemCommandRunner;
        this.systemCommand = systemCommand;
        this.commandId = commandId;
    }

    @Override
    public Boolean call() throws Exception {

        systemCommandRunner.run(systemCommand, commandId);

        return true;
    }
}
