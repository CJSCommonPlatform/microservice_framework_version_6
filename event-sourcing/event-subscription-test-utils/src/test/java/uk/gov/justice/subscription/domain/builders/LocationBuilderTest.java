package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.subscription.domain.builders.LocationBuilder.location;

import uk.gov.justice.subscription.domain.eventsource.Location;

import java.util.Optional;

import org.junit.Test;

public class LocationBuilderTest {

    @Test
    public void shouldBuildALocation() throws Exception {

        final String jmsUri = "jmsUri";
        final String restUri = "restUri";
        final String dataSource = "dataSource";

        final Location location = location()
                .withJmsUri(jmsUri)
                .withRestUri(restUri)
                .withDataSource(dataSource)
                .build();

        assertThat(location.getJmsUri(), is(jmsUri));
        assertThat(location.getRestUri(), is(restUri));
        assertThat(location.getDataSource(), is(Optional.of(dataSource)));
    }
}
