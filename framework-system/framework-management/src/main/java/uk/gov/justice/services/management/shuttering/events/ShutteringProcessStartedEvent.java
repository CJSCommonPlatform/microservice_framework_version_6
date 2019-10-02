package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ShutteringProcessStartedEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime shutteringStartedAt;

    public ShutteringProcessStartedEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime shutteringStartedAt) {
        this.commandId = commandId;
        this.target = target;
        this.shutteringStartedAt = shutteringStartedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getShutteringStartedAt() {
        return shutteringStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ShutteringProcessStartedEvent)) return false;
        final ShutteringProcessStartedEvent that = (ShutteringProcessStartedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(shutteringStartedAt, that.shutteringStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, shutteringStartedAt);
    }

    @Override
    public String toString() {
        return "ShutteringProcessStartedEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", shutteringStartedAt=" + shutteringStartedAt +
                '}';
    }
}
