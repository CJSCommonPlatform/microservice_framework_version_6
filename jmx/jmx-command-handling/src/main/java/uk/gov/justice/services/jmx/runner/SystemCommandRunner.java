package uk.gov.justice.services.jmx.runner;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.NEVER;

import uk.gov.justice.services.framework.utilities.exceptions.StackTraceProvider;
import uk.gov.justice.services.jmx.api.SystemCommandFailedException;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.command.SystemCommandStore;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;

public class SystemCommandRunner {

    @Inject
    private SystemCommandStore systemCommandStore;

    @Inject
    private StackTraceProvider stackTraceProvider;

    @Inject
    private Logger logger;

    @Transactional(NEVER)
    public boolean isSupported(final SystemCommand systemCommand) {
        return systemCommandStore.isSupported(systemCommand);
    }

    @Transactional(NEVER)
    public void run(final SystemCommand systemCommand) {

        try {
            systemCommandStore.findCommandProxy(systemCommand).invokeCommand(systemCommand);
        } catch (final Throwable e) {
            final String message = format("Failed to run System Command '%s'", systemCommand.getName());
            logger.error(message, e);

            throw new SystemCommandFailedException(
                    message + ". Caused by " + e.getClass().getName() + ": " + e.getMessage(),
                    stackTraceProvider.getStackTrace(e));
        }
    }
}
