package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.Eventsource;
import uk.gov.justice.subscription.domain.Location;

public final class EventsourceBuilder {

    private String name;
    private Location location;

    private EventsourceBuilder() {
    }

    public static EventsourceBuilder eventsource() {
        return new EventsourceBuilder();
    }

    public EventsourceBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public EventsourceBuilder withLocation(final Location location) {
        this.location = location;
        return this;
    }

    public Eventsource build() {
        return new Eventsource(name, location);
    }
}
