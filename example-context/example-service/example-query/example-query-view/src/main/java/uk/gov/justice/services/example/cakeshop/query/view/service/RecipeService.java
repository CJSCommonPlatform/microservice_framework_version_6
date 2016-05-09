package uk.gov.justice.services.example.cakeshop.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;

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
        return recipeRepository.findById(UUID.fromString(id)).map(RecipeView::new).orElse(null);
    }

    /**
     * Find recipes by their name.
     *
     * @param name of the findRecipe to search for.
     * @return List of recipes encapsulated in an {@link RecipesView}.  Never returns null.
     */
    public RecipesView findByName(final String name) {
        return new RecipesView(isEmpty(name) ? emptyList() : recipeRepository.findByNameIgnoreCase(name).stream()
                .map(RecipeView::new)
                .collect(toList()));
    }

    /**
     * Get all recipes available.
     *
     * @return List of recipes encapsulated in an {@link RecipesView}.   Never returns null.
     */
    public RecipesView getRecipes() {
        return new RecipesView(recipeRepository.getAllRecipes().stream().map(RecipeView::new).collect(toList()));
    }

}
