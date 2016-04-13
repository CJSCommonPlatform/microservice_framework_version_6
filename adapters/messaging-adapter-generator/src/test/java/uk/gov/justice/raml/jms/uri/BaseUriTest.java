package uk.gov.justice.raml.jms.uri;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.*;

public class BaseUriTest {
    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new BaseUri("message://event/listener/message/service1").tier(), is("listener"));
        assertThat(new BaseUri("message://event/processor/message/service2").tier(), is("processor"));
    }
    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new BaseUri("message://event/listener/message/service1").pillar(), is("event"));
        assertThat(new BaseUri("message://command/handler/message/service2").pillar(), is("command"));
    }

    @Test
    public void shouldReturnService() throws Exception {
        assertThat(new BaseUri("message://event/listener/message/service1").service(), is("service1"));
        assertThat(new BaseUri("message://command/handler/message/service2").service(), is("service2"));
    }
    @Test
    public void shouldReturnAdapterClientId() {
        assertThat(new BaseUri("message://event/listener/message/service1").adapterClientId(), is("service1.event.listener"));
        assertThat(new BaseUri("message://event/processor/message/service2").adapterClientId(), is("service2.event.processor"));
    }

    @Test
    public void shouldReturnTrueIfValidBaseUri() {
        assertTrue(BaseUri.valid("message://event/listener/message/service1"));
        assertTrue(BaseUri.valid("message://event/processor/message/service2"));
        assertTrue(BaseUri.valid("message://command/controller/message/service2"));
    }

    @Test
    public void shouldReturnTrueIfNotValidBaseUri() {
        assertFalse(BaseUri.valid("message://INVALID/listener/message/service1"));
        assertFalse(BaseUri.valid("message://event/INVALID/message/service2"));
        assertFalse(BaseUri.valid("message://command/controller/message"));
    }


}