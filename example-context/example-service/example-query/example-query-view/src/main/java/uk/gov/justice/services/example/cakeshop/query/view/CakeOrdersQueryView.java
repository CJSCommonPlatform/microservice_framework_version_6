package uk.gov.justice.services.example.cakeshop.query.view;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.service.CakeOrderService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.QUERY_VIEW)
public class CakeOrdersQueryView {

    private static final String NAME_RESPONSE_ORDER = "example.findOrder-response";

    private static final String FIELD_ORDER_ID = "orderId";
    private final CakeOrderService service;
    private final Enveloper enveloper;

    @Inject
    public CakeOrdersQueryView(final CakeOrderService service, final Enveloper enveloper) {
        this.service = service;
        this.enveloper = enveloper;
    }

    @Handles("example.get-order")
    public JsonEnvelope findOrder(final JsonEnvelope query) {
        return enveloper.withMetadataFrom(query, NAME_RESPONSE_ORDER).apply(
                service.findOrder(query.payloadAsJsonObject().getString(FIELD_ORDER_ID)));
    }
}
