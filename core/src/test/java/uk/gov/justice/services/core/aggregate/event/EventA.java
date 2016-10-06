package uk.gov.justice.services.core.aggregate.event;

import uk.gov.justice.domain.annotation.Event;

@Event("eventA")
public class EventA {
    private static final long serialVersionUID = 10000000001L;

    private String name;

    public EventA() {

    }

    public EventA(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
