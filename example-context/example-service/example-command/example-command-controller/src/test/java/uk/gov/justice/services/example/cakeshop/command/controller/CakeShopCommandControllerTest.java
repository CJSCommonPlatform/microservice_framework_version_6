package uk.gov.justice.services.example.cakeshop.command.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeShopCommandControllerTest {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_ORDER_ID = "orderId";
    private static final String FIELD_DELIVERY_ID = "deliveryDate";
    private static final ZonedDateTime DELIVERY_DATE = ZonedDateTime.now();
    private static final UUID RANDOM_UUID_1 = UUID.randomUUID();
    private static final UUID RANDOM_UUID_2 = UUID.randomUUID();
    private static final UUID RANDOM_UUID_3 = UUID.randomUUID();

    @Mock
    JsonEnvelope envelope;
    @Mock
    JsonObject payload;
    @Mock
    private Sender sender;
    @InjectMocks
    private CakeShopCommandController cakeShopCommandController;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RANDOM_UUID_1.toString());
        cakeShopCommandController.makeCake(envelope);

        verify(sender, times(1)).send(envelope);
    }

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RANDOM_UUID_1.toString());

        cakeShopCommandController.addRecipe(envelope);

        verify(sender, times(1)).send(envelope);
    }

    @Test
    public void shouldHandleOrderCakeCommand() throws Exception {

        JsonObject payloadWithZTD = Json.createObjectBuilder()
                .add(FIELD_ORDER_ID, RANDOM_UUID_2.toString())
                .add(FIELD_RECIPE_ID, RANDOM_UUID_3.toString())
                .add(FIELD_DELIVERY_ID, DELIVERY_DATE.toString())
                .build();

        when(envelope.payloadAsJsonObject()).thenReturn(payloadWithZTD);
        ArgumentCaptor<JsonEnvelope> result = ArgumentCaptor.forClass(JsonEnvelope.class);

        cakeShopCommandController.orderCake(envelope);

        verify(sender).send(result.capture());
        verify(sender, times(1)).send(envelope);

        assertTrue(result.getValue().payloadAsJsonObject().equals(payloadWithZTD));
    }

}
