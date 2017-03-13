package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToIngredientsConverter;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.IngredientRepository;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class RecipeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeEventListener.class);
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_PHOTO_ID = "photoId";

    @Inject
    JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    RecipeAddedToRecipeConverter recipeAddedToRecipeConverter;

    @Inject
    RecipeAddedToIngredientsConverter recipeAddedToIngredientsConverter;

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    IngredientRepository ingredientRepository;

    @Handles("example.recipe-added")
    public void recipeAdded(final JsonEnvelope event) {

        final String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        LOGGER.trace("=============> Inside add-recipe Event Listener. RecipeId: " + recipeId);

        final RecipeAdded recipeAdded = jsonObjectConverter.convert(event.payloadAsJsonObject(), RecipeAdded.class);

        recipeRepository.save(recipeAddedToRecipeConverter.convert(recipeAdded));

        LOGGER.trace("=====================================================> Recipe saved, RecipeId: " + recipeId);

        for (final Ingredient ingredient : recipeAddedToIngredientsConverter.convert(recipeAdded)) {
            if (ingredientRepository.findByNameIgnoreCase(ingredient.getName()).isEmpty()) {
                LOGGER.trace("=============> Inside add-recipe Event Listener about to save Ingredient Id: " + ingredient.getId());
                ingredientRepository.save(ingredient);
                LOGGER.trace("=====================================================> Ingredient saved, Ingredient Id: " + ingredient.getId());
            } else {
                LOGGER.trace("=====================================================> Skipped adding ingredient as it already exists, Ingredient Name: " + ingredient.getName());
            }
        }
    }

    @Handles("example.recipe-renamed")
    public void recipeRenamed(final JsonEnvelope event) {

        final String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        final String recipeName = event.payloadAsJsonObject().getString("name");
        LOGGER.trace("=============> Inside rename-recipe Event Listener. RecipeId: " + recipeId);

        final Recipe recipe = recipeRepository.findBy(UUID.fromString(recipeId));
        recipe.setName(recipeName);
        recipeRepository.save(recipe);
    }

    @Handles("example.recipe-removed")
    public void recipeRemoved(final JsonEnvelope event) {
        final String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        LOGGER.trace("=============> Inside remove-recipe Event Listener about to find recipeId: " + recipeId);
        final Recipe recipeFound = recipeRepository.findBy(UUID.fromString(recipeId));
        LOGGER.trace("=============> Found remove-recipe Event Listener. RecipeId: " + recipeFound);
        recipeRepository.remove(recipeFound);
    }

    @Handles("example.recipe-photograph-added")
    public void recipePhotographAdded(final JsonEnvelope event) {

        final String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        final String photoId = event.payloadAsJsonObject().getString(FIELD_PHOTO_ID);
        LOGGER.trace("=============> Inside recipe-photograph-added Event Listener. RecipeId: " + recipeId);

        final Recipe recipe = recipeRepository.findBy(UUID.fromString(recipeId));
        recipe.setPhotoId(UUID.fromString(photoId));
        recipeRepository.save(recipe);
    }

}
