package uk.gov.justice.services.example.cakeshop.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("example.recipe-removed")
public class RecipeRemoved {

    private final UUID recipeId;

    public RecipeRemoved(final UUID recipeId) {
        this.recipeId = recipeId;
      }

    public UUID getRecipeId() {
        return recipeId;
    }

}
