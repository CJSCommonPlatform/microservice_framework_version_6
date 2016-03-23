package uk.gov.justice.services.example.cakeshop.command.controller;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

@ServiceComponent(COMMAND_CONTROLLER)
public class CakeShopCommandController {

    private static final Logger LOGGER = getLogger(CakeShopCommandController.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Sender sender;

    @Handles("cakeshop.commands.add-recipe")
    public void addRecipe(final Envelope command) {
        LOGGER.info("=============> Inside add-recipe Command Controller. RecipeId: " + command.payload().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }

    @Handles("cakeshop.commands.make-cake")
    public void makeCake(final Envelope command) {
        LOGGER.info("=============> Inside make-cake Command Controller. RecipeId: " + command.payload().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }


}
