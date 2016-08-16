package uk.gov.justice.services.example.cakeshop.persistence;

import static java.text.MessageFormat.format;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe_;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

@Repository
public abstract class RecipeRepository extends AbstractEntityRepository<Recipe, UUID> implements CriteriaSupport<Recipe> {

    /**
     * Find all {@link Recipe} using criteria.
     *
     * @param pageSize   max size of returned result
     * @param name       to retrieve the recipe by.
     * @param glutenFree flag to retrieve gluten free recipes.
     * @return List of matching recipes. Never returns null.
     */
    public List<Recipe> findBy(final int pageSize, final Optional<String> name, Optional<Boolean> glutenFree) {
        Criteria<Recipe, Recipe> criteria = criteria();
        if (name.isPresent()) {
            criteria = criteria.like(Recipe_.name, format("%{0}%", name.get()));
        }
        if (glutenFree.isPresent()) {
            criteria = criteria.eq(Recipe_.glutenFree, glutenFree.get());
        }

        return criteria
                .orderAsc(Recipe_.name)
                .createQuery()
                .setMaxResults(pageSize)
                .getResultList();
    }


}
