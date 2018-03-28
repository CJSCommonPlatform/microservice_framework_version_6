package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.EventsourceBuilder.eventsource;

import uk.gov.justice.subscription.domain.Eventsource;
import uk.gov.justice.subscription.domain.Location;

import org.junit.Test;

public class EventsourceBuilderTest {

    @Test
    public void shouldBuildAnEventsource() throws Exception {

        final String name = "name";
        final Location location = mock(Location.class);

        final Eventsource eventsource = eventsource()
                .withName(name)
                .withLocation(location)
                .build();

        assertThat(eventsource.getName(), is(name));
        assertThat(eventsource.getLocation(), is(location));
    }
}
