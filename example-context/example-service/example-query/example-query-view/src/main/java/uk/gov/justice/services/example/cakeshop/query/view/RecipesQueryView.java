package uk.gov.justice.services.example.cakeshop.query.view;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.RecipeService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(Component.QUERY_VIEW)
public class RecipesQueryView {

    static final String NAME_RESPONSE_RECIPE = "cakeshop.query.findRecipe-response";
    static final String NAME_RESPONSE_RECIPE_LIST = "cakeshop.query.recipes-response";
    private static final Logger LOGGER = getLogger(RecipesQueryView.class);
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_NAME = "name";

    @Inject
    RecipeService recipeService;

    @Inject
    Enveloper enveloper;

    @Handles("cakeshop.query.recipe")
    public JsonEnvelope findRecipe(final JsonEnvelope query) {
        LOGGER.info("=============> Inside findRecipe Query View. RecipeId: " + query.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE).apply(
                recipeService.findRecipe(query.payloadAsJsonObject().getString(FIELD_RECIPE_ID)));
    }

    @Handles("cakeshop.query.recipes")
    public JsonEnvelope listRecipes(final JsonEnvelope query) {
        LOGGER.info("=============> Inside listRecipes Query View");

        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE_LIST).apply(fetchRecipes(query));
    }

    private RecipesView fetchRecipes(final JsonEnvelope query) {
        return query.payloadAsJsonObject().containsKey(FIELD_NAME)
                ? recipeService.findByName(query.payloadAsJsonObject().getString(FIELD_NAME))
                : recipeService.getRecipes();
    }
}
