package uk.gov.justice.services.management.suspension.executors;

import static uk.gov.justice.services.management.suspension.api.SuspensionResult.suspensionSucceeded;

import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class CommandApiSuspender implements Suspendable {

    @Inject
    private CommandApiSuspensionBean commandApiSuspensionBean;

    @Inject
    private Logger logger;

    @Override
    public boolean shouldSuspend() {
        return true;
    }

    @Override
    public boolean shouldUnsuspend() {
        return true;
    }

    @Override
    public SuspensionResult suspend(final UUID commandId, final SuspensionCommand suspensionCommand) {

        logger.info("Suspending Command API");

        commandApiSuspensionBean.suspend();

        logger.info("Suspension of Command API complete");

        return suspensionSucceeded(
                getName(),
                commandId,
                "Command API suspended with no errors",
                suspensionCommand
        );
    }

    @Override
    public SuspensionResult unsuspend(final UUID commandId, final SuspensionCommand suspensionCommand) {

        logger.info("Unsuspending Command API");

        commandApiSuspensionBean.unsuspend();

        logger.info("Unsuspension of Command API complete");

        return suspensionSucceeded(
                getName(),
                commandId,
                "Command API unsuspended with no errors",
                suspensionCommand
        );
    }
}
