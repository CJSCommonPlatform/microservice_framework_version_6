package uk.gov.justice.services.domain.aggregate;


import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.services.domain.event.SomethingHappened;

import java.util.UUID;
import java.util.stream.Stream;

public class AggregateWithParameters implements uk.gov.justice.domain.aggregate.Aggregate {

    private UUID id;

    public Stream<Object> checkOrderParameters(final UUID id,
                                               final int intvalue,
                                               final String stringValue,
                                               final boolean booleanValue) {
        return apply(Stream.of(new SomethingHappened(id)));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(SomethingHappened.class).apply(x -> {
                    this.id = x.getId();
                }));
    }
}

