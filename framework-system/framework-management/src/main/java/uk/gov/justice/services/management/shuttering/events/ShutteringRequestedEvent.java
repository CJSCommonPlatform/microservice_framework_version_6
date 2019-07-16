package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringRequestedEvent {

    private final SystemCommand target;
    private final ZonedDateTime shutteringRequestedAt;

    public ShutteringRequestedEvent(final SystemCommand systemCommand, final ZonedDateTime shutteringRequestedAt) {
        this.target = systemCommand;
        this.shutteringRequestedAt = shutteringRequestedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getShutteringRequestedAt() {
        return shutteringRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ShutteringRequestedEvent that = (ShutteringRequestedEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(shutteringRequestedAt, that.shutteringRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, shutteringRequestedAt);
    }

    @Override
    public String toString() {
        return "ShutteringRequestedEvent{" +
                "target=" + target +
                ", shutteringRequestedAt=" + shutteringRequestedAt +
                '}';
    }
}
