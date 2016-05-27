package uk.gov.justice.services.example.cakeshop.persistence;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface RecipeRepository extends EntityRepository<Recipe, UUID> {

    /**
     * Find all {@link Recipe} by recipeName (case-insensitive). Accepts '%' wildcard values.
     *
     * @param recipeName to retrieve the recipe by, including wildcard characters.
     * @return List of matching recipes. Never returns null.
     */
    @Query(value = "FROM Recipe r WHERE LOWER(r.name) LIKE LOWER(?1)")
    public List<Recipe> findByNameIgnoreCase(final String recipeName);
}
