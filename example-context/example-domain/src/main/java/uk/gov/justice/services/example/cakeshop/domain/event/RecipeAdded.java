package uk.gov.justice.services.example.cakeshop.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;

import java.util.List;
import java.util.UUID;

@Event("example.events.recipe-added")
public class RecipeAdded {

    private final UUID recipeId;
    private final String name;
    private final List<Ingredient> ingredients;
    private final Boolean glutenFree;

    public RecipeAdded(final UUID recipeId, final String name, final Boolean glutenFree, final List<Ingredient> ingredients) {
        this.recipeId = recipeId;
        this.name = name;
        this.glutenFree = glutenFree;
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

    public Boolean isGlutenFree() {
        return glutenFree;
    }
}
