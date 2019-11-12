package uk.gov.justice.services.management.suspension.process;

import static java.lang.String.format;
import static uk.gov.justice.services.management.suspension.api.SuspensionResult.suspensionFailed;

import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class SuspensionFailedHandler {

    @Inject
    private Logger logger;

    public SuspensionResult onSuspensionFailed(
            final UUID commandId,
            final SuspensionCommand suspensionCommand,
            final Suspendable suspendable,
            final Throwable exception) {

        final String message = format(
                "%s failed for %s. %s: %s",
                suspensionCommand.getName(),
                suspendable.getName(),
                exception.getClass().getName(),
                exception.getMessage());

        logger.error(message, exception);

        return suspensionFailed(
                suspendable.getName(),
                commandId,
                message,
                suspensionCommand,
                exception
        );
    }
}
