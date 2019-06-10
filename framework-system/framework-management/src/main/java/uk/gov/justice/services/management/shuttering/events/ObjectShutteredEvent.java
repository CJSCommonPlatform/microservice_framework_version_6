package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ObjectShutteredEvent {

    private final ZonedDateTime objectShutteredAt;
    private final SystemCommand systemCommand;

    public ObjectShutteredEvent(final SystemCommand systemCommand, final ZonedDateTime objectShutteredAt) {
        this.systemCommand = systemCommand;
        this.objectShutteredAt = objectShutteredAt;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public ZonedDateTime getObjectShutteredAt() {
        return objectShutteredAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ObjectShutteredEvent that = (ObjectShutteredEvent) o;
        return Objects.equals(objectShutteredAt, that.objectShutteredAt) &&
                Objects.equals(systemCommand, that.systemCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectShutteredAt, systemCommand);
    }

    @Override
    public String toString() {
        return "ObjectShutteredEvent{" +
                "objectShutteredAt=" + objectShutteredAt +
                ", systemCommand=" + systemCommand +
                '}';
    }
}


