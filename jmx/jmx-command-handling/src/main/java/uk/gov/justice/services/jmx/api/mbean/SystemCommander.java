package uk.gov.justice.services.jmx.api.mbean;

import static java.lang.String.format;

import uk.gov.justice.services.jmx.api.SystemCommandException;
import uk.gov.justice.services.jmx.api.SystemCommandInvocationException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.state.ApplicationManagementState;
import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.jmx.command.SystemCommandScanner;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private Logger logger;

    @Inject
    private SystemCommandStore systemCommandStore;

    @Inject
    private SystemCommandScanner systemCommandScanner;

    @Inject
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    @Override
    public void call(final SystemCommand systemCommand) {

        final String commandName = systemCommand.getName();
        logger.info(format("Received System Command '%s'", commandName));

        try {
            systemCommandStore.findCommandProxy(systemCommand).invokeCommand(systemCommand);
        } catch (final SystemCommandInvocationException e) {
            final String message = format("Failed to run System Command '%s'", commandName);
            logger.error(message, e);
            throw new SystemCommandException(message, e);
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
