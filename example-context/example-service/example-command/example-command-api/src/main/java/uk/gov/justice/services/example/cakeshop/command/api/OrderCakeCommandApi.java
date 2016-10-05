package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class OrderCakeCommandApi {

    @Inject
    Sender sender;

    @Handles("example.order-cake")
    public void orderCake(final JsonEnvelope command) {
        sender.send(command);
    }
}
