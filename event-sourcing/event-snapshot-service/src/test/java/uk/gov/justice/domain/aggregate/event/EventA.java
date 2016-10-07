package uk.gov.justice.domain.aggregate.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;

@Event("eventA")
public class EventA implements Serializable {
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
