package uk.gov.justice.subscription.domain.eventsource;

public class EventSourceDefinition {

    private final String name;
    private final Location location;

    public EventSourceDefinition(final String name, final Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
