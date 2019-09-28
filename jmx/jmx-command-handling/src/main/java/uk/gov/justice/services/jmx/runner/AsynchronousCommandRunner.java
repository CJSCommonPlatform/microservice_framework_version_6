package uk.gov.justice.services.jmx.runner;

import static java.util.UUID.randomUUID;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

public class AsynchronousCommandRunner {

    @Resource
    private ManagedExecutorService managedExecutorService;

    @Inject
    private SystemCommandRunner systemCommandRunner;


    public UUID run(final SystemCommand systemCommand) {

        final UUID commandId = randomUUID();
        final RunSystemCommandTask runSystemCommandTask = new RunSystemCommandTask(
                systemCommandRunner,
                systemCommand,
                commandId
        );

        managedExecutorService.submit(runSystemCommandTask);

        return commandId;
    }

    public boolean isSupported(final SystemCommand systemCommand) {
        return systemCommandRunner.isSupported(systemCommand);
    }
}
