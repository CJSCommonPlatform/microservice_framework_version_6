package uk.gov.justice.services.example.cakeshop.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("example.events.recipe-photograph-added")
public class RecipePhotographAdded {

    private final UUID recipeId;
    private final UUID photoId;

    public RecipePhotographAdded(final UUID recipeId, final UUID photoId) {
        this.recipeId = recipeId;
        this.photoId = photoId;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public UUID getPhotoId() {
        return photoId;
    }
}
