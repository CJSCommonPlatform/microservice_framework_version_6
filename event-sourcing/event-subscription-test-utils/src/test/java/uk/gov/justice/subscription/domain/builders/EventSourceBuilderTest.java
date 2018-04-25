package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.Location;

import org.junit.Test;

public class EventSourceBuilderTest {

    @Test
    public void shouldBuildAnEventsource() throws Exception {

        final String name = "name";
        final Location location = mock(Location.class);

        final EventSource eventsource = eventsource()
                .withName(name)
                .withLocation(location)
                .build();

        assertThat(eventsource.getName(), is(name));
        assertThat(eventsource.getLocation(), is(location));
    }
}
