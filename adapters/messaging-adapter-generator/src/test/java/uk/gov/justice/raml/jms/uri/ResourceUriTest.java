package uk.gov.justice.raml.jms.uri;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ResourceUriTest {

    @Test
    public void shouldReturnContext() throws Exception {
       assertThat(new ResourceUri("/contextAbc.handler.commands").context(), is("contextAbc"));
        assertThat(new ResourceUri("/context2.events").context(), is("context2"));
    }

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new ResourceUri("/contextAbc.controller.commands").pillar(), is("commands"));
        assertThat(new ResourceUri("/context2.events").pillar(), is("events"));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new ResourceUri("/contextAbc.handler.commands").tier(), is("handler"));
        assertThat(new ResourceUri("/context2.events").tier(), nullValue());
    }

    @Test
    public void shouldReturnStringRepresentation() {
        assertThat(new ResourceUri("/contextAbc.handler.commands").toString(), is("/contextAbc.handler.commands"));
        assertThat(new ResourceUri("/context2.events").toString(), is("/context2.events"));
    }

    @Test
    public void shouldCheckIfValidUri() {
        assertTrue(ResourceUri.valid("/contextAbc.controller.commands"));
        assertTrue(ResourceUri.valid("/structure.handler.commands"));
        assertTrue(ResourceUri.valid("/aaa.events"));

        assertFalse(ResourceUri.valid("/aaa.InValid.commands"));
        assertFalse(ResourceUri.valid("/contextAbc.handler.events"));
        assertFalse(ResourceUri.valid("/controller.commands"));

    }

}