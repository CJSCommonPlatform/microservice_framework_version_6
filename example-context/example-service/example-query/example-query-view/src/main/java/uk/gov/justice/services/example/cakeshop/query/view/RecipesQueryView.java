package uk.gov.justice.services.example.cakeshop.query.view;

import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonObjects.getBoolean;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.query.view.request.SearchRecipes;
import uk.gov.justice.services.example.cakeshop.query.view.response.PhotoView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.RecipeService;
import uk.gov.justice.services.messaging.Envelope;

import java.util.Optional;

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

    @Inject
    RecipeService recipeService;

    @Handles("example.get-recipe")
    public Envelope<RecipeView> findRecipe(final Envelope<JsonObject> query) {
        final RecipeView recipe = recipeService.findRecipe(query.payload().getString(FIELD_RECIPE_ID));

        return envelop(recipe)
                .withName(NAME_RESPONSE_RECIPE)
                .withMetadataFrom(query);
    }

    @Handles("example.search-recipes")
    public Envelope<RecipesView> listRecipes(final Envelope<JsonObject> query) {
        return envelop(fetchRecipes(query))
                .withName(NAME_RESPONSE_RECIPE_LIST)
                .withMetadataFrom(query);
    }

    @Handles("example.query-recipes")
    public Envelope<RecipesView> queryRecipes(final Envelope<SearchRecipes> query) {
        return envelop(fetchRecipes(query.payload()))
                .withName(NAME_RESPONSE_RECIPE_LIST)
                .withMetadataFrom(query);
    }

    @Handles("example.get-recipe-photograph")
    public Envelope<PhotoView> findRecipePhoto(final Envelope<JsonObject> query) {
        final PhotoView photo = recipeService.findRecipePhoto(query.payload().getString(FIELD_RECIPE_ID));
        return envelop(photo)
                .withName(NAME_RESPONSE_RECIPE_PHOTO)
                .withMetadataFrom(query);
    }

    private RecipesView fetchRecipes(final Envelope<JsonObject> query) {
        final JsonObject queryObject = query.payload();
        return recipeService.getRecipes(
                queryObject.getInt(PAGESIZE),
                getString(queryObject, FIELD_NAME),
                getBoolean(queryObject, FIELD_GLUTEN_FREE));
    }

    private RecipesView fetchRecipes(final SearchRecipes searchRecipes) {
        return recipeService.getRecipes(
                searchRecipes.getPagesize(),
                Optional.of(searchRecipes.getName()),
                Optional.of(searchRecipes.isGlutenFree()));
    }
}