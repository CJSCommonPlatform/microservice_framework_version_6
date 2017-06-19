package uk.gov.justice.services.generators.commons.helper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.generators.commons.helper.MessagingBaseUri;

import org.junit.Test;

public class MessagingBaseUriTest {
    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(new MessagingBaseUri("message://event/listener/message/service1").tier(), is("listener"));
        assertThat(new MessagingBaseUri("message://event/processor/message/service2").tier(), is("processor"));
    }

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(new MessagingBaseUri("message://event/listener/message/service1").pillar(), is("event"));
        assertThat(new MessagingBaseUri("message://command/handler/message/service2").pillar(), is("command"));
    }

    @Test
    public void shouldReturnService() throws Exception {
        assertThat(new MessagingBaseUri("message://event/listener/message/service1").service(), is("service1"));
        assertThat(new MessagingBaseUri("message://command/handler/message/service2").service(), is("service2"));
    }

    @Test
    public void shouldReturnAdapterClientId() {
        assertThat(new MessagingBaseUri("message://event/listener/message/service1").adapterClientId(), is("service1.event.listener"));
        assertThat(new MessagingBaseUri("message://event/processor/message/service2").adapterClientId(), is("service2.event.processor"));
    }

    @Test
    public void shouldReturnTrueIfValidBaseUri() {
        assertTrue(MessagingBaseUri.valid("message://event/listener/message/service1"));
        assertTrue(MessagingBaseUri.valid("message://event/processor/message/service2"));
        assertTrue(MessagingBaseUri.valid("message://command/controller/message/service2"));
    }

    @Test
    public void shouldReturnTrueIfNotValidBaseUri() {
        assertFalse(MessagingBaseUri.valid("message://INVALID/listener/message/service1"));
        assertFalse(MessagingBaseUri.valid("message://event/INVALID/message/service2"));
        assertFalse(MessagingBaseUri.valid("message://command/controller/message"));
    }


    @Test
    public void shouldReturnComponent() {
        assertThat(new MessagingBaseUri("message://event/listener/message/service1").component(), is(EVENT_LISTENER));
        assertThat(new MessagingBaseUri("message://event/processor/message/service2").component(), is(EVENT_PROCESSOR));
    }

    @Test
    public void shouldReturnClassName() {
        assertThat(new MessagingBaseUri("message://event/listener/message/system").toClassName(), is("SystemEventListener"));
    }
}
