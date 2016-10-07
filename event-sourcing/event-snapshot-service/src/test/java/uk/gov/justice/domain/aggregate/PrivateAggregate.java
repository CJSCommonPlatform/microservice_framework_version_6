package uk.gov.justice.domain.aggregate;


import uk.gov.justice.domain.aggregate.Aggregate;

public class PrivateAggregate implements Aggregate {
    private static final long serialVersionUID = 42L;

    private PrivateAggregate() {

    }

    @Override
    public Object apply(Object event) {
        return event;
    }
}