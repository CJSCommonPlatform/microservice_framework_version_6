package uk.gov.justice.services.jmx.runner;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class AsynchronousCommandRunner {

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private SystemCommandRunner systemCommandRunner;


    public void run(final SystemCommand systemCommand) {

        final RunSystemCommandTask runSystemCommandTask = new RunSystemCommandTask(
                systemCommandRunner,
                systemCommand
        );

        managedExecutorService.submit(runSystemCommandTask);
    }

    public boolean isSupported(final SystemCommand systemCommand) {
        return systemCommandRunner.isSupported(systemCommand);
    }
}
