package uk.gov.justice.services.example.cakeshop.persistence;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Repository for {@link Recipe}
 */
public class RecipeRepository {

    private static final String LIKE_PATTERN = "%%s%";

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Save {@link Recipe}
     *
     * @param recipe to be persisted.
     */
    @Transactional
    public void save(final Recipe recipe) {
        entityManager.persist(recipe);
    }

    /**
     * Find {@link Recipe} by id.
     *
     * @param id of the recipe to retrieve.
     * @return recipe.
     */
    public Optional<Recipe> findById(final UUID id) {
        return Optional.ofNullable(entityManager.find(Recipe.class, id));
    }

    /**
     * Find all {@link Recipe} by recipeName (case-insensitive). Accepts '%' wildcard values.
     *
     * @param recipeName to retrieve the recipe by, including wildcard characters.
     * @return List of matching recipes. Never returns null.
     */
    public List<Recipe> findByNameIgnoreCase(final String recipeName) {
        List<Recipe> result = entityManager.createNamedQuery("Recipe.findByNameIgnoreCase")
                .setParameter("name", "%" + recipeName.toLowerCase() + "%"/*String.format(LIKE_PATTERN, recipeName.toLowerCase()*/)
                .getResultList();

        return result == null ? Collections.emptyList() : result;
    }

    /**
     * Get all of the {@link Recipe}'s.
     *
     * @return List of all recipes.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> result = entityManager.createNamedQuery("Recipe.getRecipes")
                .getResultList();

        return result == null ? Collections.emptyList() : result;
    }

}
