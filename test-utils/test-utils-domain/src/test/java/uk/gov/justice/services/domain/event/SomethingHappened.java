package uk.gov.justice.services.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("something-happened")
public class SomethingHappened {

    private final UUID id;

    public SomethingHappened(final UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
