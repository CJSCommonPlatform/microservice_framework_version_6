package uk.gov.justice.subscription.domain;

public class Eventsource {

    private final String name;
    private final Location location;

    public Eventsource(final String name, final Location location) {
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
