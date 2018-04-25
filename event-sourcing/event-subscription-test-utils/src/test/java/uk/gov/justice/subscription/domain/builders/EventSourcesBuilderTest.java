package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourcesBuilder.eventSources;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.EventSources;

import org.junit.Test;

public class EventSourcesBuilderTest {

    @Test
    public void shouldBuildEventSources() throws Exception {

        final EventSource event_source_1 = mock(EventSource.class);
        final EventSource event_source_2 = mock(EventSource.class);

        final EventSources eventSources = eventSources()
                .withEventSources(asList(event_source_1, event_source_2))
                .build();

        assertThat(eventSources.getEventSources(), hasItem(event_source_1));
        assertThat(eventSources.getEventSources(), hasItem(event_source_2));
    }

    @Test
    public void shouldBeAbleToAddOneEventSourceAtATime() throws Exception {

        final EventSource event_source_1 = mock(EventSource.class);
        final EventSource event_source_2 = mock(EventSource.class);

        final EventSources eventSources = eventSources()
                .withEventSource(event_source_1)
                .withEventSource(event_source_2)
                .build();

        assertThat(eventSources.getEventSources(), hasItem(event_source_1));
        assertThat(eventSources.getEventSources(), hasItem(event_source_2));
    }
}