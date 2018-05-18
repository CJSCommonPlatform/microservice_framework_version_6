package uk.gov.justice.subscription.registry;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceDefinitionRegistryTest {

    @Test
    public void shouldRegisterStreamOfEventSourceDefinitions() throws Exception {

        final EventSourceDefinition eventSourceDefinition1 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition1").build();

        final EventSourceDefinition eventSourceDefinition2 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition2").build();

        final Stream<EventSourceDefinition> eventSourceList = Stream.of(eventSourceDefinition1, eventSourceDefinition2);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry, is(notNullValue()));

        assertThat(eventSourceRegistry.getEventSourceFor("eventSourceDefinition1").get(), is(eventSourceDefinition1));
        assertThat(eventSourceRegistry.getEventSourceFor("eventSourceDefinition2").get(), is(eventSourceDefinition2));
    }

    @Test
    public void shouldIgnoreSecondEventSourceDefinitionForTheSameName() throws Exception {

        final EventSourceDefinition eventSourceDefinition1 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition1").build();

        final EventSourceDefinition eventSourceDefinition2 = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition1").build();


        final Stream<EventSourceDefinition> eventSourceList = Stream.of(eventSourceDefinition1, eventSourceDefinition2);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry, is(notNullValue()));

        assertThat(eventSourceRegistry.getEventSourceFor("eventSourceDefinition1").get(), is(sameInstance(eventSourceDefinition1)));
    }


    @Test
    public void shouldReturnEmptyIfNoEventSourceDefinitionIsFoundForASpecifiedServiceName() throws Exception {

        final EventSourceDefinition eventSourceDefinition = eventsource()
                .withLocation(mock(Location.class))
                .withName("eventSource").build();

        final Stream<EventSourceDefinition> eventSourceList = Stream.of(eventSourceDefinition);

        final EventSourceRegistry eventSourceRegistry = new EventSourceRegistry(eventSourceList);

        assertThat(eventSourceRegistry.getEventSourceFor("nonExistentEventSource"), is(empty()));
    }
}