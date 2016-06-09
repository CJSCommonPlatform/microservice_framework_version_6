package uk.gov.justice.services.example.cakeshop.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

/**
 * Converter to convert the {@link RecipeAdded} 'event' into the relevant view entities (e.g. {@link
 * Recipe}.
 */
public class RecipeAddedToRecipeConverter implements Converter<RecipeAdded, Recipe> {

    @Override
    public Recipe convert(final RecipeAdded source) {
        return new Recipe(source.getRecipeId(), source.getName());
    }
}
