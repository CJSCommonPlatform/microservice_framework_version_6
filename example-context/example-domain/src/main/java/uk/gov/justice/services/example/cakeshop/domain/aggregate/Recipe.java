package uk.gov.justice.services.example.cakeshop.domain.aggregate;

import static uk.gov.justice.domain.aggregate.condition.Precondition.assertPrecondition;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Recipe aggregate.
 */
public class Recipe implements Aggregate {

    private UUID recipeId;

    public Stream<Object> addRecipe(final UUID recipeId, final String name, final List<Ingredient> ingredients) {
        assertPrecondition(this.recipeId == null).orElseThrow("Recipe already added");

        return apply(Stream.of(new RecipeAdded(recipeId, name, ingredients)));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(RecipeAdded.class).apply(x -> recipeId = x.getRecipeId()));
    }
}
