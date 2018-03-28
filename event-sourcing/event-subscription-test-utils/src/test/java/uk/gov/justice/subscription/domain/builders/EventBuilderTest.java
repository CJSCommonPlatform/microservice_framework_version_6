package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.subscription.domain.builders.EventBuilder.event;

import uk.gov.justice.subscription.domain.Event;

import org.junit.Assert;
import org.junit.Test;

public class EventBuilderTest {

    @Test
    public void shouldBuildAnEvent() throws Exception {

        final String name = "event name";
        final String schemaUri = "schemaUri";

        final Event event = event()
                .withName(name)
                .withSchemaUri(schemaUri)
                .build();

        assertThat(event.getName(), is(name));
        assertThat(event.getSchemaUri(), is(schemaUri));
    }
}
