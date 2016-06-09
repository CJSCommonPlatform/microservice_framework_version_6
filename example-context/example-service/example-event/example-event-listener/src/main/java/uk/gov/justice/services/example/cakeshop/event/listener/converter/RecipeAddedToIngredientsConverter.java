package uk.gov.justice.services.example.cakeshop.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Converter to convert the {@link RecipeAdded} 'event' into the relevant view entities (e.g. {@link
 * Ingredient}.
 */
public class RecipeAddedToIngredientsConverter implements Converter<RecipeAdded, List<Ingredient>> {

    @Override
    public List<Ingredient> convert(final RecipeAdded source) {
        final List<Ingredient> ingredients = new ArrayList<>();
        source.getIngredients().forEach(i -> ingredients.add(new Ingredient(UUID.randomUUID(), i.getName())));

        return ingredients;
    }
}
