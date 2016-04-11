package uk.gov.justice.services.example.cakeshop.query.api;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.Envelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(QUERY_API)
public class RecipesQueryApi {

    private static final Logger LOGGER = getLogger(RecipesQueryApi.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Requester requester;

    @Handles("cakeshop.queries.recipes")
    public Envelope listRecipes(final Envelope query) {
        LOGGER.info("=============> Inside listRecipes Query API");

        return requester.request(query);
    }

    @Handles("cakeshop.queries.recipe")
    public Envelope recipe(final Envelope query) {
        LOGGER.info("=============> Inside recipe Query API. RecipeId: " + query.payload().getString(FIELD_RECIPE_ID));

        return requester.request(query);
    }
}