package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_API)
public class OrderCakeCommandApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCakeCommandApi.class);

    @Inject
    Sender sender;

    @Handles("cakeshop.order-cake")
    public void orderCake(final JsonEnvelope command) {
        LOGGER.info("=============> Inside order-cake Command API. ");

        sender.send(command);
    }
}
