package uk.gov.justice.services.example.cakeshop.command.api;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrderCakeCommandApiTest {

    private static final String FIELD_ORDER_ID = "orderId";
    private static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    JsonEnvelope envelope;
    @Mock
    JsonObject payload;
    @Mock
    private Sender sender;
    @InjectMocks
    private OrderCakeCommandApi orderCakeCommandApi;

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_ORDER_ID)).thenReturn(ORDER_ID.toString());

        orderCakeCommandApi.orderCake(envelope);
        verify(sender, times(1)).send(envelope);
    }
}
