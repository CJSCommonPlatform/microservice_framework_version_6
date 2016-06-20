package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MessagingResourceUriTest {

    @Test
    public void shouldReturnContext() throws Exception {
        assertThat(new MessagingResourceUri("/contextAbc.handler.command").context(), is("contextAbc"));
        assertThat(new MessagingResourceUri("/context2.event").context(), is("context2"));
    }

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new MessagingResourceUri("/contextAbc.controller.command").pillar(), is("command"));
        assertThat(new MessagingResourceUri("/context2.event").pillar(), is("event"));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new MessagingResourceUri("/contextAbc.handler.command").tier(), is("handler"));
        assertThat(new MessagingResourceUri("/context2.event").tier(), nullValue());
    }

    @Test
    public void shouldReturnStringRepresentation() {
        assertThat(new MessagingResourceUri("/contextAbc.handler.command").toString(), is("/contextAbc.handler.command"));
        assertThat(new MessagingResourceUri("/context2.event").toString(), is("/context2.event"));
    }

    @Test
    public void shouldCheckIfValidUri() {
        assertTrue(MessagingResourceUri.valid("/contextAbc.controller.command"));
        assertTrue(MessagingResourceUri.valid("/structure.handler.command"));
        assertTrue(MessagingResourceUri.valid("/aaa.event"));

        assertFalse(MessagingResourceUri.valid("/aaa.InValid.command"));
        assertFalse(MessagingResourceUri.valid("/contextAbc.handler.event"));
        assertFalse(MessagingResourceUri.valid("/controller.command"));

    }

}