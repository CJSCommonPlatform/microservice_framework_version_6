package uk.gov.justice.services.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.domain.event.SomethingHappened;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


public class StatelessAggregate implements Aggregate {

    private UUID id;

    public Stream<Object> doSomething(final UUID id) {
        return apply(Stream.of(new SomethingHappened(id)));
    }

    public Stream<Object> doNotDoSomething(final UUID id) {
        return apply(Stream.of());
    }

    public Stream<Object> doSomethingTwice(final UUID id) {
        final List<SomethingHappened> values = new ArrayList<>();
        values.add(new SomethingHappened(id));
        values.add(new SomethingHappened(id));
        return apply(Stream.of(new SomethingHappened(id), new SomethingHappened(id)));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(SomethingHappened.class).apply(x -> {
                    this.id = x.getId();
                }));
    }
}
