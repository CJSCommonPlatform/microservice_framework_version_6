package uk.gov.justice.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.util.stream.Stream;

public class IllegalAccessTestAggregate implements Aggregate {
    private static final long serialVersionUID = 10000000001L;

    private IllegalAccessTestAggregate() {

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
