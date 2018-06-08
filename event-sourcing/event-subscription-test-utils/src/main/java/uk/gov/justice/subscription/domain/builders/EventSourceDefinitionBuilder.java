package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

public final class EventSourceDefinitionBuilder {

    private String name;
    private boolean is_default;
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

    public EventSourceDefinitionBuilder withDefault(final boolean is_default) {
        this.is_default = is_default;
        return this;
    }

    public EventSourceDefinition build() {
        return new EventSourceDefinition(name, is_default, location);
    }
}
