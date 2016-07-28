package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MessagingResourceUriTest {

    @Test
    public void shouldReturnStringRepresentation() {
        assertThat(new MessagingResourceUri("/contextAbc.handler.command").toString(), is("/contextAbc.handler.command"));
        assertThat(new MessagingResourceUri("/context2.event").toString(), is("/context2.event"));
    }

    @Test
    public void shouldReturnCapitalisedStringRepresentation() {
        assertThat(new MessagingResourceUri("/people.handler.command").toCapitalisedString(), is("PeopleHandlerCommand"));
        assertThat(new MessagingResourceUri("/public.event").toCapitalisedString(), is("PublicEvent"));
    }


}