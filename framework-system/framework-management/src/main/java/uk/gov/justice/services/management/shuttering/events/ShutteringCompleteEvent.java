package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ShutteringCompleteEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime shutteringCompleteAt;

    public ShutteringCompleteEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime shutteringCompleteAt) {
        this.commandId = commandId;
        this.target = target;
        this.shutteringCompleteAt = shutteringCompleteAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getShutteringCompleteAt() {
        return shutteringCompleteAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ShutteringCompleteEvent)) return false;
        final ShutteringCompleteEvent that = (ShutteringCompleteEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(shutteringCompleteAt, that.shutteringCompleteAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, shutteringCompleteAt);
    }

    @Override
    public String toString() {
        return "ShutteringCompleteEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", shutteringCompleteAt=" + shutteringCompleteAt +
                '}';
    }
}
