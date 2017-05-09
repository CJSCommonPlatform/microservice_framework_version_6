package uk.gov.justice.services.domain.aggregate;


import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.domain.event.SomethingElseHappened;
import uk.gov.justice.services.domain.event.SomethingHappened;

import java.util.UUID;
import java.util.stream.Stream;

public class StatefulAggregate implements Aggregate {

    private UUID id;

    private boolean eventApplied;

    public Stream<Object> doSomething(final UUID id) {
        if (eventApplied){
            return apply(Stream.of(new SomethingElseHappened(id)));
        }
        return apply(Stream.of(new SomethingHappened(id)));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(SomethingHappened.class).apply(x -> {
                    this.eventApplied = true;
                    this.id = x.getId();
                }),
                when(SomethingElseHappened.class).apply(x -> {
                    this.id = x.getId();
                }));
    }
}
