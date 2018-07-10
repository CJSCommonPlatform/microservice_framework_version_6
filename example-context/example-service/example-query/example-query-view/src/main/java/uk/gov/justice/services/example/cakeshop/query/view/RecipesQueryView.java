package uk.gov.justice.services.example.cakeshop.query.view;

import static uk.gov.justice.services.messaging.JsonObjects.getBoolean;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.PhotoView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.RecipeService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.QUERY_VIEW)
public class RecipesQueryView {

    static final String NAME_RESPONSE_RECIPE = "example.get-recipe";
    static final String NAME_RESPONSE_RECIPE_PHOTO = "example.get-recipe-photograph";
    static final String NAME_RESPONSE_RECIPE_LIST = "example.search-recipes";
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_NAME = "name";
    private static final String PAGESIZE = "pagesize";
    private static final String FIELD_GLUTEN_FREE = "glutenFree";

    private final RecipeService recipeService;
    private final Enveloper enveloper;

    @Inject
    public RecipesQueryView(RecipeService recipeService, Enveloper enveloper) {
        this.recipeService = recipeService;
        this.enveloper = enveloper;
    }

    @Handles("example.get-recipe")
    public JsonEnvelope findRecipe(final JsonEnvelope query) {
        final RecipeView recipe = recipeService.findRecipe(query.payloadAsJsonObject().getString(FIELD_RECIPE_ID));
        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE).apply(
                recipe);
    }

    @Handles("example.search-recipes")
    public JsonEnvelope listRecipes(final JsonEnvelope query) {
        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE_LIST).apply(fetchRecipes(query));
    }

    @Handles("example.query-recipes")
    public JsonEnvelope queryRecipes(final JsonEnvelope query) {
        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE_LIST).apply(fetchRecipes(query));
    }

    @Handles("example.get-recipe-photograph")
    public JsonEnvelope findRecipePhoto(final JsonEnvelope query) {
        final PhotoView photo = recipeService.findRecipePhoto(query.payloadAsJsonObject().getString(FIELD_RECIPE_ID));
        return enveloper.withMetadataFrom(query, NAME_RESPONSE_RECIPE_PHOTO).apply(photo);
    }

    private RecipesView fetchRecipes(final JsonEnvelope query) {
        final JsonObject queryObject = query.payloadAsJsonObject();
        return recipeService.getRecipes(
                queryObject.getInt(PAGESIZE),
                getString(queryObject, FIELD_NAME),
                getBoolean(queryObject, FIELD_GLUTEN_FREE));
    }
}