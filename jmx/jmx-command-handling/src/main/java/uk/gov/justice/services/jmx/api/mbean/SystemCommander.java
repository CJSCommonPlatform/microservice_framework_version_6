package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.UnsupportedSystemCommandException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.state.ApplicationManagementState;
import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.runner.AsynchronousCommandRunner;

import java.util.List;

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
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    @Override
    public void call(final SystemCommand systemCommand) {

        logger.info(format("Received System Command '%s'", systemCommand.getName()));

        if(asynchronousCommandRunnerBean.isSupported(systemCommand)) {
            asynchronousCommandRunnerBean.run(systemCommand);
        } else {
           throw new UnsupportedSystemCommandException(format("The system command '%s' is not supported on this context.", systemCommand.getName()));
        }
    }

    @Override
    public List<SystemCommand> listCommands() {
       return systemCommandScanner.findCommands();
    }

    @Override
    public ApplicationManagementState getApplicationState() {
        return applicationManagementStateRegistry.getApplicationManagementState();
    }
}
