package uk.gov.justice.services.example.cakeshop.query.view;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakeOrderView;
import uk.gov.justice.services.example.cakeshop.query.view.service.CakeOrderService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeOrdersQueryViewTest {

    private CakeOrdersQueryView queryView;

    @Mock
    private CakeOrderService service;

    @Before
    public void setup() {
        final Enveloper enveloper = new Enveloper(new ObjectToJsonValueConverter(new ObjectMapperProducer().objectMapper()));
        queryView = new CakeOrdersQueryView(service, enveloper);
    }

    @Test
    public void shouldReturnOrder() {

        final UUID orderId = UUID.randomUUID();
        final UUID recipeId = UUID.randomUUID();
        final ZonedDateTime deliveryDate = ZonedDateTime.now();

        when(service.findOrder(orderId.toString())).thenReturn(new CakeOrderView(orderId, recipeId, deliveryDate));

        final JsonEnvelope response = queryView.findOrder(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf(orderId.toString(), "orderId").build());

        assertThat(response.payloadAsJsonObject().getString("orderId"), equalTo(orderId.toString()));
        assertThat(response.payloadAsJsonObject().getString("recipeId"), equalTo(recipeId.toString()));


    }

}
