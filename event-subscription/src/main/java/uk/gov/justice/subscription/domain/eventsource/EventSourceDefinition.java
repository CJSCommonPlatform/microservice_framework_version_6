package uk.gov.justice.subscription.domain.eventsource;

public class EventSourceDefinition {

    private final String name;
    private final boolean is_default;
    private final Location location;

    public EventSourceDefinition(final String name,
                                 final boolean is_default,
                                 final Location location) {
        this.name = name;
        this.is_default = is_default;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isDefault() {
        return is_default;
    }
}
