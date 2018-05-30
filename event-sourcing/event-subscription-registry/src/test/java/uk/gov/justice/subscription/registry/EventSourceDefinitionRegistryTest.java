package uk.gov.justice.subscription.registry;

import static java.util.Optional.empty;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceDefinitionRegistryTest {

    @Test
    public void shouldReturnEventSourceDefinition() {

        final EventSourceDefinition eventSourceDefinition1 = eventSourceDefinition()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition1")
                .build();

        final EventSourceDefinition eventSourceDefinition2 = eventSourceDefinition()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition2").build();


        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();

        eventSourceDefinitionRegistry.register(eventSourceDefinition1);
        eventSourceDefinitionRegistry.register(eventSourceDefinition2);

        final EventSourceDefinition defaultEventSourceDefinition = eventSourceDefinitionRegistry.getEventSourceDefinitionFor("eventSourceDefinition1");

        assertThat(defaultEventSourceDefinition, is(eventSourceDefinition1));
    }

    @Test
    public void shouldReturnDefaultEventSourceDefinition() {

        final Location location = new Location("", "", Optional.of("dataSource"));

        final EventSourceDefinition eventSourceDefinition1 = eventSourceDefinition()
                .withLocation(location)
                .withName("eventSourceDefinition1")
                .withDefaultEventSource(true)
                .build();

        final EventSourceDefinition eventSourceDefinition2 = eventSourceDefinition()
                .withLocation(mock(Location.class))
                .withName("eventSourceDefinition2")
                .build();

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();

        eventSourceDefinitionRegistry.register(eventSourceDefinition1);
        eventSourceDefinitionRegistry.register(eventSourceDefinition2);


        final EventSourceDefinition defaultEventSourceDefinition = eventSourceDefinitionRegistry.getDefaultEventSourceDefinition();

        assertThat(defaultEventSourceDefinition.getName(), is("eventSourceDefinition1"));
        assertTrue(defaultEventSourceDefinition.isDefaultEventSource());
    }

    @Test
    public void shouldThrowExceptionIfSecondDefaultEventSourceIsAdded() {

        final Location location1 = new Location("", "", Optional.of("dataSource"));
        final Location location2 = new Location("", "", Optional.of("dataSource"));

        final EventSourceDefinition eventSourceDefinition1 = eventSourceDefinition()
                .withLocation(location1)
                .withName("eventSourceDefinition1")
                .withDefaultEventSource(true)
                .build();

        final EventSourceDefinition eventSourceDefinition2 = eventSourceDefinition()
                .withLocation(location2)
                .withDefaultEventSource(true)
                .withName("eventSourceDefinition2").build();


        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();

        try {
            eventSourceDefinitionRegistry.register(eventSourceDefinition1);
            eventSourceDefinitionRegistry.register(eventSourceDefinition2);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("You cannot define more than one default event source in event-sources.yaml"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoDataSourceDefinedForDefaultEventSource() {

        final Location location1 = new Location("", "", empty());
        final Location location2 = new Location("", "", empty());

        final EventSourceDefinition eventSourceDefinition1 = eventSourceDefinition()
                .withLocation(location1)
                .withName("eventSourceDefinition1")
                .withDefaultEventSource(true)
                .build();

        final EventSourceDefinition eventSourceDefinition2 = eventSourceDefinition()
                .withLocation(location2)
                .withDefaultEventSource(false)
                .withName("eventSourceDefinition2").build();

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();

        try {
            eventSourceDefinitionRegistry.register(eventSourceDefinition1);
            eventSourceDefinitionRegistry.register(eventSourceDefinition2);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("You must define data_source for default event source in event-sources.yaml"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoDefaultDataSourceDefined() {

        final EventSourceDefinition eventSourceDefinition1 = eventSourceDefinition()
                .withName("eventSourceDefinition1")
                .build();

        final EventSourceDefinition eventSourceDefinition2 = eventSourceDefinition()
                .withName("eventSourceDefinition2").build();

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();

        try {
            eventSourceDefinitionRegistry.register(eventSourceDefinition1);
            eventSourceDefinitionRegistry.register(eventSourceDefinition2);
            //Test
            eventSourceDefinitionRegistry.getDefaultEventSourceDefinition();
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("You must define a default event source in event-sources.yaml"));
        }
    }

}