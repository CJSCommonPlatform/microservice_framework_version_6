package uk.gov.justice.services.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("something-else-happened")
public class SomethingElseHappened {

    private final UUID id;

    public SomethingElseHappened(final UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
