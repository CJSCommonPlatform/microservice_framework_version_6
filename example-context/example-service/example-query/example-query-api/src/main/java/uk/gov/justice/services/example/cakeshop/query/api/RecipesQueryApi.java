package uk.gov.justice.services.example.cakeshop.query.api;

import org.slf4j.Logger;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

@ServiceComponent(QUERY_API)
public class RecipesQueryApi {

    private static final Logger LOGGER = getLogger(RecipesQueryApi.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Requester requester;

    @Handles("cakeshop.query.recipes")
    public JsonEnvelope listRecipes(final JsonEnvelope query) {
        LOGGER.info("=============> Inside listRecipes Query API");

        return requester.request(query);
    }

    @Handles("cakeshop.query.recipe")
    public JsonEnvelope recipe(final JsonEnvelope query) {
        LOGGER.info("=============> Inside recipe Query API. RecipeId: " + query.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        return requester.request(query);
    }
}
