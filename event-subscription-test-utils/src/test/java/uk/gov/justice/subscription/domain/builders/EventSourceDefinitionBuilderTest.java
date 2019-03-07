package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import org.junit.Test;

public class EventSourceDefinitionBuilderTest {

    @Test
    public void shouldBuildAnEventSourceDefinition() throws Exception {

        final String name = "name";
        final Location location = mock(Location.class);

        final EventSourceDefinition eventsource = eventSourceDefinition()
                .withName(name)
                .withLocation(location)
                .withDefault(true)
                .build();

        assertThat(eventsource.getName(), is(name));
        assertThat(eventsource.isDefault(),is(true));
        assertThat(eventsource.getLocation(), is(location));
    }
}
