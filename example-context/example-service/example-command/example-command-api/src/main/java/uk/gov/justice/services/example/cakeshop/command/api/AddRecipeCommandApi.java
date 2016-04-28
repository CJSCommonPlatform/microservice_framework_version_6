package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ServiceComponent(COMMAND_API)
public class AddRecipeCommandApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddRecipeCommandApi.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Sender sender;

    @Handles("cakeshop.command.add-recipe")
    public void addRecipe(final JsonEnvelope command) {
        LOGGER.info("=============> Inside add-recipe Command API. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }

}
