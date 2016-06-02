package uk.gov.justice.services.example.cakeshop.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;

import java.util.List;
import java.util.UUID;

@Event("cakeshop.events.recipe-added-transacted")
public class TransactedRecipeAdded {

    private final UUID recipeId;
    private final String name;
    private final List<Ingredient> ingredients;

    public TransactedRecipeAdded(final UUID recipeId, final String name, final List<Ingredient> ingredients) {
        this.recipeId = recipeId;
        this.name = name;
        this.ingredients = ingredients;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

}
