package uk.gov.justice.services.jmx.command;

import static java.lang.String.format;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SystemCommander implements SystemCommanderMBean {

    @Inject
    private Logger logger;

    @Inject
    private SystemCommandStore systemCommandStore;

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
}
