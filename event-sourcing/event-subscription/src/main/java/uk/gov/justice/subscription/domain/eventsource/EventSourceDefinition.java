package uk.gov.justice.subscription.domain.eventsource;

public class EventSourceDefinition {

    private final String name;
    private final boolean defaultEventSource;
    private final Location location;

    public EventSourceDefinition(final String name,
                                 final boolean defaultEventSource,
                                 final Location location
    ) {
        this.name = name;
        this.defaultEventSource = defaultEventSource;
        this.location = location;
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
