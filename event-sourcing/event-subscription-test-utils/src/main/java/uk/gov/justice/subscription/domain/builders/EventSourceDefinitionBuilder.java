package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

public final class EventSourceDefinitionBuilder {

    private String name;
    private boolean defaultEventSource;
    private Location location;

    private EventSourceDefinitionBuilder() {
    }

    public static EventSourceDefinitionBuilder eventSourceDefinition() {
        return new EventSourceDefinitionBuilder();
    }

    public EventSourceDefinitionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public EventSourceDefinitionBuilder withLocation(final Location location) {
        this.location = location;
        return this;
    }

    public EventSourceDefinitionBuilder withDefaultEventSource(final boolean defaultEventSource) {
        this.defaultEventSource = defaultEventSource;
        return this;
    }

    public EventSourceDefinition build() {
        return new EventSourceDefinition(name,defaultEventSource, location);
    }
}
