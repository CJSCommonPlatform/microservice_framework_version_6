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
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_CAKE_ID = "cakeId";

    @Inject
    Sender sender;

    @Handles("cakeshop.command.add-recipe")
    public void addRecipe(final JsonEnvelope command) {
        LOGGER.info("=============> Inside add-recipe Command Controller. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }

    @Handles("cakeshop.command.add-recipe-transacted")
    public void addRecipeTransacted(final JsonEnvelope command) {
        LOGGER.info("=============> Inside add-recipe-transacted Command Controller. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }

    @Handles("cakeshop.command.make-cake")
    public void makeCake(final JsonEnvelope command) {
        LOGGER.info("=============> Inside make-cake Command Controller. CakeId: " + command.payloadAsJsonObject().getString(FIELD_CAKE_ID));

        sender.send(command);
    }


}
