package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class UnshutteringRequestedEvent {

    private final ZonedDateTime unshutteringRequestedAt;
    private final UUID commandId;
    private final SystemCommand target;

    public UnshutteringRequestedEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime unshutteringRequestedAt) {
        this.commandId = commandId;
        this.target = target;
        this.unshutteringRequestedAt = unshutteringRequestedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getUnshutteringRequestedAt() {
        return unshutteringRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UnshutteringRequestedEvent that = (UnshutteringRequestedEvent) o;
        return Objects.equals(unshutteringRequestedAt, that.unshutteringRequestedAt) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unshutteringRequestedAt, target);
    }

    @Override
    public String toString() {
        return "UnshutteringRequestedEvent{" +
                "target=" + unshutteringRequestedAt +
                ", target=" + target +
                '}';
    }
}
