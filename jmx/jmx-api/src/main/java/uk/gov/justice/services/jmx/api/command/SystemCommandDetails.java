package uk.gov.justice.services.jmx.api.command;

import java.io.Serializable;

public class SystemCommandDetails implements Serializable {

    private final String name;
    private final String description;

    public SystemCommandDetails(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
