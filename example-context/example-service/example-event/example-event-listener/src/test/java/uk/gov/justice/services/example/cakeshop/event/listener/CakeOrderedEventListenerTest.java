package uk.gov.justice.services.example.cakeshop.event.listener;


import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.builder.JsonEnvelopeBuilder.envelopeWithDefaultMetadata;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.persistence.CakeOrderRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.CakeOrder;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeOrderedEventListenerTest {

    @Mock
    private CakeOrderRepository repository;

    @Mock
    JsonObjectToObjectConverter converter;

    @InjectMocks
    CakeOrderedEventListener listener;


    @Test
    public void shouldSaveEvent() throws Exception {

        final String orderId = UUID.randomUUID().toString();
        final String recipeId = UUID.randomUUID().toString();
        final String deliveryDate = "2016-07-25T13:09:01Z";

        final CakeOrder cakeOrderObject = new CakeOrder(UUID.randomUUID(), UUID.randomUUID(), ZonedDateTime.now());
        when(converter.convert(createObjectBuilder()
                .add("orderId", orderId).add("recipeId", recipeId).add("deliveryDate", deliveryDate).build(), CakeOrder.class))
                .thenReturn(cakeOrderObject);


        listener.handle(envelopeWithDefaultMetadata().withPayloadOf("orderId", orderId, "recipeId", recipeId, "deliveryDate", deliveryDate).build());

        verify(repository).save(cakeOrderObject);

    }
}