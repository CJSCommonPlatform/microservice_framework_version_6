package uk.gov.justice.services.example.cakeshop.command.api;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrderCakeCommandApiTest {

    @Mock
    private Sender sender;

    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    private OrderCakeCommandApi commandApi;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeCaptor;

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {
        assertThat(OrderCakeCommandApi.class, isHandlerClass(COMMAND_API)
                .with(method("orderCake")
                        .thatHandles("example.order-cake")));
    }

    @Test
    public void shouldHandleOrderCakeRequest() {
        commandApi = new OrderCakeCommandApi();
        commandApi.enveloper = enveloper;
        commandApi.sender = sender;

        final JsonEnvelope envelope = envelope()
                .with(metadataWithDefaults().withName("example.order-cake"))
                .withPayloadOf("Field", "Value").build();

        commandApi.orderCake(envelope);

        verify(sender).send(envelopeCaptor.capture());
        assertThat(envelopeCaptor.getValue().metadata().name(), is("example.command.order-cake"));
    }
}
