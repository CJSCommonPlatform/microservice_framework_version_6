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
public class RecipesQueryController {

    private static final Logger LOGGER = getLogger(RecipesQueryController.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Requester requester;

    @Handles("cakeshop.search-recipes")
    public JsonEnvelope listRecipes(final JsonEnvelope query) {
        LOGGER.info("=============> Inside listRecipes Query Controller");

        return requester.request(query);
    }

    @Handles("cakeshop.get-recipe")
    public JsonEnvelope recipe(final JsonEnvelope query) {
        LOGGER.info("=============> Inside recipe Query Controller. RecipeId: " + query.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        return requester.request(query);
    }
}
