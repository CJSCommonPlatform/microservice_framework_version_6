package uk.gov.justice.domain.aggregate.event;

import uk.gov.justice.domain.annotation.Event;

@Event("eventC")
public class EventC {
    private static final long serialVersionUID = 10000000001L;

    private String name;

    public EventC() {

    }

    public EventC(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}