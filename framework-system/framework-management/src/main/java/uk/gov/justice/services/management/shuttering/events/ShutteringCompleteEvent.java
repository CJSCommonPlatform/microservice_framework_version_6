package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringCompleteEvent {

    private final SystemCommand target;
    private final ZonedDateTime shutteringCompleteAt;

    public ShutteringCompleteEvent(final SystemCommand target, final ZonedDateTime shutteringCompleteAt) {
        this.target = target;
        this.shutteringCompleteAt = shutteringCompleteAt;
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
        return Objects.equals(target, that.target) &&
                Objects.equals(shutteringCompleteAt, that.shutteringCompleteAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, shutteringCompleteAt);
    }

    @Override
    public String toString() {
        return "ShutteringCompleteEvent{" +
                "target=" + target +
                ", shutteringCompleteAt=" + shutteringCompleteAt +
                '}';
    }
}
