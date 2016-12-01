package uk.gov.justice.services.example.cakeshop.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;

import java.util.List;
import java.util.UUID;

@Event("example.recipe-renamed")
public class RecipeRenamed {

    private final UUID recipeId;
    private final String name;

    public RecipeRenamed(final UUID recipeId, final String name) {
        this.recipeId = recipeId;
        this.name = name;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }
}
