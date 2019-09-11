package uk.gov.justice.services.jmx.runner;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.concurrent.Callable;

public class RunSystemCommandTask implements Callable<Boolean> {

    private final SystemCommandRunner systemCommandRunner;
    private final SystemCommand systemCommand;

    public RunSystemCommandTask(final SystemCommandRunner systemCommandRunner, final SystemCommand systemCommand) {
        this.systemCommandRunner = systemCommandRunner;
        this.systemCommand = systemCommand;
    }

    @Override
    public Boolean call() throws Exception {

        systemCommandRunner.run(systemCommand);

        return true;
    }
}
