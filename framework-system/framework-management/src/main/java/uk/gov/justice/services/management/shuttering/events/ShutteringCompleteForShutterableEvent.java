package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringCompleteForShutterableEvent {

    private final ZonedDateTime shutteringCompleteAt;
    private final SystemCommand target;
    private final Class<?> shutterable;

    public ShutteringCompleteForShutterableEvent(final ZonedDateTime shutteringStartedAt, final SystemCommand target, final Class<?> shutterable) {
        this.shutteringCompleteAt = shutteringStartedAt;
        this.target = target;
        this.shutterable = shutterable;
    }

    public ZonedDateTime getShutteringCompleteAt() {
        return shutteringCompleteAt;
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
        if (!(o instanceof ShutteringCompleteForShutterableEvent)) return false;
        final ShutteringCompleteForShutterableEvent that = (ShutteringCompleteForShutterableEvent) o;
        return Objects.equals(shutteringCompleteAt, that.shutteringCompleteAt) &&
                Objects.equals(target, that.target) &&
                Objects.equals(shutterable, that.shutterable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shutteringCompleteAt, target, shutterable);
    }

    @Override
    public String toString() {
        return "ShutteringCompleteForShutterableEvent{" +
                "shutteringCompleteAt=" + shutteringCompleteAt +
                ", target=" + target +
                ", shutterable=" + shutterable +
                '}';
    }
}
