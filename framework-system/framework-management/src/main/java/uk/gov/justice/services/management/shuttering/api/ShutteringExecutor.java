package uk.gov.justice.services.management.shuttering.api;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.util.UUID;

public interface ShutteringExecutor {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default boolean shouldShutter() {
        return false;
    }

    default boolean shouldUnshutter() {
        return false;
    }

    default ShutteringResult shutter(final UUID commandId, final SystemCommand systemCommand) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    default ShutteringResult unshutter(final UUID commandId, final SystemCommand systemCommand) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
