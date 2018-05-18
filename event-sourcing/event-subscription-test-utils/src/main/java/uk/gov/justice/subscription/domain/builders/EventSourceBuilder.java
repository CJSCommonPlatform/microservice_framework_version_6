package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

public final class EventSourceBuilder {

    private String name;
    private Location location;

    private EventSourceBuilder() {
    }

    public static EventSourceBuilder eventsource() {
        return new EventSourceBuilder();
    }

    public EventSourceBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public EventSourceBuilder withLocation(final Location location) {
        this.location = location;
        return this;
    }

    public EventSourceDefinition build() {
        return new EventSourceDefinition(name, location);
    }
}
