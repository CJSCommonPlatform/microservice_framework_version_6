package uk.gov.justice.services.example.cakeshop.query.controller;


import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(QUERY_CONTROLLER)
public class CakeOrdersQueryController {
    private static final Logger LOGGER = getLogger(CakeOrdersQueryController.class);

    private static final String FIELD_ORDER_ID = "orderId";

    @Inject
    Requester requester;

    @Handles("cakeshop.get-order")
    public JsonEnvelope getOrder(final JsonEnvelope query) {
        LOGGER.info("=============> Inside cake order Query API. OrderId: " + query.payloadAsJsonObject().getString(FIELD_ORDER_ID));

        return requester.request(query);
    }
}
