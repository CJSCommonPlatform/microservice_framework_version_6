package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.util.ArrayList;
import java.util.List;

public class TestAggregate implements Aggregate {

    private static final long serialVersionUID = 10000000001L;

    public String state;

    public List<Object> recordedEvents = new ArrayList<>();

    public TestAggregate() {
    }

    public TestAggregate(String state) {
        this.state = state;
    }

    @Override
    public Object apply(Object event) {

        recordedEvents.add(event);
        return event;
    }
}