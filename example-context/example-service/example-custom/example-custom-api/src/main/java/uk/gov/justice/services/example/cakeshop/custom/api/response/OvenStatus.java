package uk.gov.justice.services.example.cakeshop.custom.api.response;

import java.util.UUID;

public class OvenStatus {

    private final String name;
    private final UUID id;
    private final int temperature;
    private final boolean active;

    public OvenStatus(final UUID id, final String name, final int temperature, final boolean active) {
        this.name = name;
        this.id = id;
        this.temperature = temperature;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public int getTemperature() {
        return temperature;
    }

    public boolean isActive() {
        return active;
    }
}
