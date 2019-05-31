package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ObjectUnshutteredEvent {

    private final ZonedDateTime objectUnshutteredAt;
    private final SystemCommand systemCommand;

    public ObjectUnshutteredEvent(final SystemCommand systemCommand, final ZonedDateTime objectUnshutteredAt) {
        this.systemCommand = systemCommand;
        this.objectUnshutteredAt = objectUnshutteredAt;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public ZonedDateTime getObjectUnshutteredAt() {
        return objectUnshutteredAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ObjectUnshutteredEvent that = (ObjectUnshutteredEvent) o;
        return Objects.equals(objectUnshutteredAt, that.objectUnshutteredAt) &&
                Objects.equals(systemCommand, that.systemCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectUnshutteredAt, systemCommand);
    }

    @Override
    public String toString() {
        return "ObjectUnshutteredEvent{" +
                "objectUnshutteredAt=" + objectUnshutteredAt +
                ", systemCommand=" + systemCommand +
                '}';
    }
}
