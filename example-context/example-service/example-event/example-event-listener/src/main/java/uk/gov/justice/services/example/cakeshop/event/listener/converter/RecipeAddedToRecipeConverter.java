package uk.gov.justice.services.example.cakeshop.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.persistence.entity.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter to convert the {@link RecipeAdded} 'event' into the relevant view entities (e.g. {@link
 * Recipe}.
 */
public class RecipeAddedToRecipeConverter implements Converter<RecipeAdded, Recipe> {

    @Override
    public Recipe convert(final RecipeAdded source) {
        final List<RecipeIngredient> ingredients = new ArrayList<>();
        source.getIngredients().forEach(i -> ingredients.add(new RecipeIngredient(null, source.getRecipeId(), i.getName(), i.getQuantity())));

        return new Recipe(source.getRecipeId(), source.getName());
    }
}
