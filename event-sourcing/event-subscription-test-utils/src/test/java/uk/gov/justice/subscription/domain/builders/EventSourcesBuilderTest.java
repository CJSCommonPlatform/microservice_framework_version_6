package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourcesBuilder.eventSources;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import org.junit.Test;

public class EventSourcesBuilderTest {

    @Test
    public void shouldBuildEventSources() throws Exception {

        final EventSourceDefinition event_source_1 = mock(EventSourceDefinition.class);
        final EventSourceDefinition event_source_2 = mock(EventSourceDefinition.class);

        final EventSourcesDefinitionCollection eventSources = eventSources()
                .withEventSources(asList(event_source_1, event_source_2))
                .build();

        assertThat(eventSources.getEventSources(), hasItem(event_source_1));
        assertThat(eventSources.getEventSources(), hasItem(event_source_2));
    }

    @Test
    public void shouldBeAbleToAddOneEventSourceAtATime() throws Exception {

        final EventSourceDefinition event_source_1 = mock(EventSourceDefinition.class);
        final EventSourceDefinition event_source_2 = mock(EventSourceDefinition.class);

        final EventSourcesDefinitionCollection eventSources = eventSources()
                .withEventSource(event_source_1)
                .withEventSource(event_source_2)
                .build();

        assertThat(eventSources.getEventSources(), hasItem(event_source_1));
        assertThat(eventSources.getEventSources(), hasItem(event_source_2));
    }
}