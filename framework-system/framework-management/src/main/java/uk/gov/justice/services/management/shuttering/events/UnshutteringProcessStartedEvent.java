package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class UnshutteringProcessStartedEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime unshutteringStartedAt;

    public UnshutteringProcessStartedEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime unshutteringStartedAt) {
        this.commandId = commandId;
        this.target = target;
        this.unshutteringStartedAt = unshutteringStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getUnshutteringStartedAt() {
        return unshutteringStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UnshutteringProcessStartedEvent)) return false;
        final UnshutteringProcessStartedEvent that = (UnshutteringProcessStartedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(unshutteringStartedAt, that.unshutteringStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, unshutteringStartedAt);
    }

    @Override
    public String toString() {
        return "UnshutteringProcessStartedEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", unshutteringStartedAt=" + unshutteringStartedAt +
                '}';
    }
}
