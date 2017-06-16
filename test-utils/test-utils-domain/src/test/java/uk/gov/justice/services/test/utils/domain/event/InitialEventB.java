package uk.gov.justice.services.test.utils.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("context.event-b")
public class InitialEventB {
    private UUID id;
    private String stringFieldB;

    public InitialEventB(final UUID id, final String stringFieldB) {
        this.id = id;
        this.stringFieldB = stringFieldB;
    }

    public UUID getId() {
        return id;
    }

    public String getStringFieldB() {
        return stringFieldB;
    }
}
