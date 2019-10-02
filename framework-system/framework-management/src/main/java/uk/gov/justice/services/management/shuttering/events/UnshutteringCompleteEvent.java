package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class UnshutteringCompleteEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime unshutteringCompletedAt;

    public UnshutteringCompleteEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime unshutteringCompletedAt) {
        this.commandId = commandId;
        this.target = target;
        this.unshutteringCompletedAt = unshutteringCompletedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getUnshutteringCompletedAt() {
        return unshutteringCompletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UnshutteringCompleteEvent)) return false;
        final UnshutteringCompleteEvent that = (UnshutteringCompleteEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(unshutteringCompletedAt, that.unshutteringCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, unshutteringCompletedAt);
    }

    @Override
    public String toString() {
        return "UnshutteringCompleteEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", unshutteringCompletedAt=" + unshutteringCompletedAt +
                '}';
    }
}
