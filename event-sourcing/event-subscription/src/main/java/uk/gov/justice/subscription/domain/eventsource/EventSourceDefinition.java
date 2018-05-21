package uk.gov.justice.subscription.domain.eventsource;

public class EventSourceDefinition {

    private final String name;
    private final Location location;
    private final boolean defaultEventSource;

    public EventSourceDefinition(final String name,
                                 final Location location,
                                 final boolean defaultEventSource) {
        this.name = name;
        this.location = location;
        this.defaultEventSource = defaultEventSource;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isDefaultEventSource() {
        return defaultEventSource;
    }

}
