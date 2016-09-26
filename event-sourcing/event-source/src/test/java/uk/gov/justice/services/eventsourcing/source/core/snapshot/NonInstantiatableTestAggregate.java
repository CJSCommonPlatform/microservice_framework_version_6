package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.util.stream.Stream;

public abstract class NonInstantiatableTestAggregate implements Aggregate {
    private static final long serialVersionUID = 10000000001L;

    public NonInstantiatableTestAggregate() {

    }

    @Override
    public Object apply(Object event) {
        return null;
    }

    @Override
    public Stream<Object> apply(Stream<Object> events) {
        return null;
    }
}
