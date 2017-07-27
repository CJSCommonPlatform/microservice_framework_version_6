package uk.gov.justice.services.example.cakeshop.query.view;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.CakeOrderView;
import uk.gov.justice.services.example.cakeshop.query.view.service.CakeOrderService;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CakeOrdersQueryViewTest {

    @Mock
    private CakeOrderService service;

    @Spy
    private Enveloper enveloper = new EnveloperFactory().create();

    @InjectMocks
    private CakeOrdersQueryView queryView;

    @Test
    public void shouldHaveCorrectHandlerMethod() throws Exception {
        assertThat(queryView, isHandler(QUERY_VIEW)
                .with(method("findOrder").thatHandles("example.get-order")));
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
