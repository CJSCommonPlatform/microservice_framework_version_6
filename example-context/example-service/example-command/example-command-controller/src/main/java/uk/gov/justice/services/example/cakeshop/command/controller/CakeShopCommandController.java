package uk.gov.justice.services.example.cakeshop.command.controller;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(COMMAND_CONTROLLER)
public class CakeShopCommandController {
    private static final Logger LOGGER = getLogger(CakeShopCommandController.class);

    @Inject
    Sender sender;

    @Handles("example.add-recipe")
    public void addRecipe(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside add-recipe Command Handler");

        sender.send(command);
    }

    @Handles("example.remove-recipe")
    public void removeRecipe(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside remove-recipe Command Handler");

        sender.send(command);
    }

    @Handles("example.make-cake")
    public void makeCake(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside make-cake Command Handler");

        sender.send(command);
    }

    @Handles("example.order-cake")
    public void orderCake(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside order-cake Command Handler");

        sender.send(command);
    }

    @Handles("example.rename-recipe")
    public void renameRecipe(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside rename-recipe Command Controller Handler");
        sender.send(command);
    }

    @Handles("example.upload-photograph")
    public void uploadPhotograph(final JsonEnvelope command) {
        LOGGER.trace("=============> Inside upload-photograph Command Controller Handler");
        sender.send(command);
    }
}
