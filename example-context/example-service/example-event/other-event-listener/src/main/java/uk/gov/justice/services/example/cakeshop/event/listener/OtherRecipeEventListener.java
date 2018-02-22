package uk.gov.justice.services.example.cakeshop.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.OtherRecipeAddedToIngredientsConverter;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.OtherRecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.IngredientRepository;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent("OTHER_EVENT_LISTENER")
public class OtherRecipeEventListener {

    @Inject
    JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    OtherRecipeAddedToRecipeConverter otherRecipeAddedToRecipeConverter;

    @Inject
    OtherRecipeAddedToIngredientsConverter recipeAddedToIngredientsConverter;

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    IngredientRepository ingredientRepository;

    @Handles("other.recipe-added")
    public void recipeAdded(final JsonEnvelope event) {

        final RecipeAdded recipeAdded = jsonObjectConverter.convert(event.payloadAsJsonObject(), RecipeAdded.class);
        recipeRepository.save(otherRecipeAddedToRecipeConverter.convert(recipeAdded));

        for (final Ingredient ingredient : recipeAddedToIngredientsConverter.convert(recipeAdded)) {
            if (ingredientRepository.findByNameIgnoreCase(ingredient.getName()).isEmpty()) {
                ingredientRepository.save(ingredient);
            }
        }
    }
}
