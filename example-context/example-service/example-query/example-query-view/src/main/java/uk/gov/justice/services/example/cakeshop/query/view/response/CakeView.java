package uk.gov.justice.services.example.cakeshop.query.view.response;

import java.util.UUID;

public class CakeView {

    private final UUID id;
    private final String name;

    public CakeView(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
