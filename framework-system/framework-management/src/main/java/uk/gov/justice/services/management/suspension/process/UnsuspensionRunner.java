package uk.gov.justice.services.management.suspension.process;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class UnsuspensionRunner {

    @Inject
    private SuspendablesProvider suspendablesProvider;

    @Inject
    private SuspensionFailedHandler suspensionFailedHandler;

    @Inject
    private Logger logger;

    public List<SuspensionResult> runUsuspension(final UUID commandId, final SuspensionCommand suspensionCommand) {
        return suspendablesProvider.getSuspendables().stream()
                .filter(Suspendable::shouldUnsuspend)
                .map(suspendable -> unsuspend(commandId, suspensionCommand, suspendable))
                .collect(toList());
    }

    private SuspensionResult unsuspend(final UUID commandId, final SuspensionCommand suspensionCommand, final Suspendable suspendable) {

        logger.info(format("Unshuttering %s", suspendable.getName()));

        try {
            return suspendable.unsuspend(commandId, suspensionCommand);
        } catch (final Throwable e) {
            return suspensionFailedHandler.onSuspensionFailed(commandId, suspensionCommand, suspendable, e);
        }
    }
}
