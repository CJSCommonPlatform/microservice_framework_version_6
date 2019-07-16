package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringStartedForShutterableEvent {

    private final ZonedDateTime shutteringStartedAt;
    private final SystemCommand target;
    private final Class<?> shutterable;

    public ShutteringStartedForShutterableEvent(final ZonedDateTime shutteringStartedAt, final SystemCommand target, final Class<?> shutterable) {
        this.shutteringStartedAt = shutteringStartedAt;
        this.target = target;
        this.shutterable = shutterable;
    }

    public ZonedDateTime getShutteringStartedAt() {
        return shutteringStartedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public Class<?> getShutterable() {
        return shutterable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ShutteringStartedForShutterableEvent)) return false;
        final ShutteringStartedForShutterableEvent that = (ShutteringStartedForShutterableEvent) o;
        return Objects.equals(shutteringStartedAt, that.shutteringStartedAt) &&
                Objects.equals(target, that.target) &&
                Objects.equals(shutterable, that.shutterable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shutteringStartedAt, target, shutterable);
    }

    @Override
    public String toString() {
        return "ShutteringStartedForShutterableEvent{" +
                "shutteringStartedAt=" + shutteringStartedAt +
                ", target=" + target +
                ", shutterable=" + shutterable +
                '}';
    }
}
