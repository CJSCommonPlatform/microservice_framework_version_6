package uk.gov.justice.subscription.registry;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.Location;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceRegistryTest {

    @Test
    public void shouldRegisterStreamOfEventSources() throws Exception {

        final EventSource eventSource1 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource1").build();

        final EventSource eventSource2 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource2").build();

        final Stream<EventSource> eventSourceList = Stream.of(eventSource1, eventSource2);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry, is(notNullValue()));

        assertThat(eventSourceRegistry.getEventSourceFor("eventSource1").get(), is(eventSource1));
        assertThat(eventSourceRegistry.getEventSourceFor("eventSource2").get(), is(eventSource2));
    }

    @Test
    public void shouldIgnoreSecondEventSourceForTheSameName() throws Exception {

        final EventSource eventSource1 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource1").build();

        final EventSource eventSource2 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource1").build();


        final Stream<EventSource> eventSourceList = Stream.of(eventSource1, eventSource2);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry, is(notNullValue()));

        assertThat(eventSourceRegistry.getEventSourceFor("eventSource1").get(), is(sameInstance(eventSource1)));
    }


    @Test
    public void shouldReturnEmptyIfNoEventSourceIsFoundForASpecifiedServiceName() throws Exception {

        final EventSource eventSource1 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource").build();

        final Stream<EventSource> eventSourceList = Stream.of(eventSource1);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry.getEventSourceFor("nonExistentEventSource"), is(empty()));
    }
}