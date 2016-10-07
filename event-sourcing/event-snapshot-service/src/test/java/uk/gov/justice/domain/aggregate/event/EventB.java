package uk.gov.justice.domain.aggregate.event;

import uk.gov.justice.domain.annotation.Event;

@Event("eventB")
public class EventB {
    private static final long serialVersionUID = 10000000001L;

    private String name;

    public EventB() {

    }

    public EventB(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
