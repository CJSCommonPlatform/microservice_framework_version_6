package uk.gov.justice.domain.aggregate;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Unit tests for the {@link Aggregate} interface.
 */
public class AggregateTest {

    @Test
    public void shouldApplyAllEventsInStream() {
        Stream<Object> events = Stream.of("eventA", "eventB");

        RecordingAggregate aggregate = new RecordingAggregate();
        aggregate.apply(events);

        assertThat(aggregate.events, hasItems("eventA", "eventB"));
    }

    @Test
    public void shouldReturnAllEventsInStream() {
        Stream<Object> events = Stream.of("eventA", "eventB");

        RecordingAggregate aggregate = new RecordingAggregate();
        Stream<Object> returnedEvents = aggregate.apply(events);

        assertThat(returnedEvents.collect(toList()), hasItems("eventA", "eventB"));
    }

    private class RecordingAggregate implements Aggregate {

        List<Object> events = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            events.add(event);
            return event;
        }
    }
}
