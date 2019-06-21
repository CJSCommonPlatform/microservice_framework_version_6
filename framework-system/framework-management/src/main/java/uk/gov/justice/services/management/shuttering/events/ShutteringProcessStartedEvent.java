package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringProcessStartedEvent {

    private final SystemCommand target;
    private final ZonedDateTime shutteringStartedAt;

    public ShutteringProcessStartedEvent(final SystemCommand target, final ZonedDateTime shutteringStartedAt) {
        this.target = target;
        this.shutteringStartedAt = shutteringStartedAt;
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
        return Objects.equals(target, that.target) &&
                Objects.equals(shutteringStartedAt, that.shutteringStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, shutteringStartedAt);
    }

    @Override
    public String toString() {
        return "ShutteringProcessStarted{" +
                "target=" + target +
                ", shutteringStartedAt=" + shutteringStartedAt +
                '}';
    }
}
