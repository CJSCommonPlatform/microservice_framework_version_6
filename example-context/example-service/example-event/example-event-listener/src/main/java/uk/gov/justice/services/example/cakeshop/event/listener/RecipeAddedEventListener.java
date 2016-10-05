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
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class RecipeAddedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeAddedEventListener.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

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
            }
            else{
                LOGGER.trace("=====================================================> Skipped adding ingredient as it already exists, Ingredient Name: " + ingredient.getName());
            }
        }
    }
}
