package uk.gov.justice.services.test.utils.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("context.event-a")
public class InitialEventA {
    private UUID id;
    private String stringFieldA;

    public InitialEventA(final UUID id, final String stringFieldA) {
        this.id = id;
        this.stringFieldA = stringFieldA;
    }

    public UUID getId() {
        return id;
    }

    public String getStringFieldA() {
        return stringFieldA;
    }
}
