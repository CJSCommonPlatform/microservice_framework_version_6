package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import org.junit.Test;

public class MessagingAdapterBaseUriTest {
    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/service1").tier(), is("listener"));
        assertThat(new MessagingAdapterBaseUri("message://event/processor/message/service2").tier(), is("processor"));
    }

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/service1").pillar(), is("event"));
        assertThat(new MessagingAdapterBaseUri("message://command/handler/message/service2").pillar(), is("command"));
    }

    @Test
    public void shouldReturnService() throws Exception {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/service1").service(), is("service1"));
        assertThat(new MessagingAdapterBaseUri("message://command/handler/message/service2").service(), is("service2"));
    }

    @Test
    public void shouldReturnAdapterClientId() {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/service1").adapterClientId(), is("service1.event.listener"));
        assertThat(new MessagingAdapterBaseUri("message://event/processor/message/service2").adapterClientId(), is("service2.event.processor"));
    }

    @Test
    public void shouldReturnTrueIfValidBaseUri() {
        assertTrue(MessagingAdapterBaseUri.valid("message://event/listener/message/service1"));
        assertTrue(MessagingAdapterBaseUri.valid("message://event/processor/message/service2"));
        assertTrue(MessagingAdapterBaseUri.valid("message://command/controller/message/service2"));
    }

    @Test
    public void shouldReturnTrueIfNotValidBaseUri() {
        assertFalse(MessagingAdapterBaseUri.valid("message://INVALID/listener/message/service1"));
        assertFalse(MessagingAdapterBaseUri.valid("message://event/INVALID/message/service2"));
        assertFalse(MessagingAdapterBaseUri.valid("message://command/controller/message"));
    }


    @Test
    public void shouldReturnComponent() {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/service1").component(), is(EVENT_LISTENER));
        assertThat(new MessagingAdapterBaseUri("message://event/processor/message/service2").component(), is(EVENT_PROCESSOR));
    }

    @Test
    public void shouldReturnClassName() {
        assertThat(new MessagingAdapterBaseUri("message://event/listener/message/system").toClassName(), is("SystemEventListener"));
    }
}
