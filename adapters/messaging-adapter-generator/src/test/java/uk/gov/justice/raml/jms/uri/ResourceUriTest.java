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
       assertThat(new ResourceUri("/contextAbc.handler.command").context(), is("contextAbc"));
        assertThat(new ResourceUri("/context2.event").context(), is("context2"));
    }

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new ResourceUri("/contextAbc.controller.command").pillar(), is("command"));
        assertThat(new ResourceUri("/context2.event").pillar(), is("event"));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new ResourceUri("/contextAbc.handler.command").tier(), is("handler"));
        assertThat(new ResourceUri("/context2.event").tier(), nullValue());
    }

    @Test
    public void shouldReturnStringRepresentation() {
        assertThat(new ResourceUri("/contextAbc.handler.command").toString(), is("/contextAbc.handler.command"));
        assertThat(new ResourceUri("/context2.event").toString(), is("/context2.event"));
    }

    @Test
    public void shouldCheckIfValidUri() {
        assertTrue(ResourceUri.valid("/contextAbc.controller.command"));
        assertTrue(ResourceUri.valid("/structure.handler.command"));
        assertTrue(ResourceUri.valid("/aaa.event"));

        assertFalse(ResourceUri.valid("/aaa.InValid.command"));
        assertFalse(ResourceUri.valid("/contextAbc.handler.event"));
        assertFalse(ResourceUri.valid("/controller.command"));

    }

}
