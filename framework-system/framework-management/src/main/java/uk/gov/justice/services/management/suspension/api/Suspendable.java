package uk.gov.justice.services.management.suspension.api;

import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

public interface Suspendable {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default boolean shouldSuspend() {
        return false;
    }

    default boolean shouldUnsuspend() {
        return false;
    }

    default SuspensionResult suspend(final UUID commandId, final SuspensionCommand suspensionCommand) throws SuspensionFailedException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default SuspensionResult unsuspend(final UUID commandId, final SuspensionCommand suspensionCommand) throws SuspensionFailedException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
