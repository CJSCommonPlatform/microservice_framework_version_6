package uk.gov.justice.services.example.cakeshop.query.view.service;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.query.view.response.PhotoView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Service to manage {@link Recipe}.
 */
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    /**
     * Find findRecipe by id.
     *
     * @param id of the findRecipe.
     * @return {@link RecipeView} representation of the found recipe, or null.
     */
    public RecipeView findRecipe(final String id) {
        return Optional.ofNullable(recipeRepository.findBy(UUID.fromString(id)))
                .map(RecipeView::new)
                .orElse(null);
    }


    /**
     * Get recipes by criteria.
     *
     * @return List of recipes encapsulated in an {@link RecipesView}.   Never returns null.
     */
    public RecipesView getRecipes(final int pageSize, final Optional<String> recipeName, Optional<Boolean> glutenFree) {
        return new RecipesView(recipeRepository.findBy(pageSize, recipeName, glutenFree).stream().map(RecipeView::new).collect(toList()));
    }

    public PhotoView findRecipePhoto(final String recipeId) {
        final Recipe recipe = recipeRepository.findBy(UUID.fromString(recipeId));
        return recipe != null && recipe.getPhotoId() != null ? new PhotoView(recipe.getPhotoId()) : null;
    }
}
