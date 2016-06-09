package uk.gov.justice.services.example.cakeshop.query.view.response;

import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;

import java.util.Objects;
import java.util.UUID;

/**
 * View representation of an {@link  Recipe}.
 */
public class RecipeView {

    private final UUID id;
    private final String name;
    private final boolean glutenFree;

    public RecipeView(final UUID id, final String name, final boolean glutenFree) {
        this.id = id;
        this.name = name;
        this.glutenFree = glutenFree;
    }

    public RecipeView(final Recipe recipe) {
        this.id = recipe.getId();
        this.name = recipe.getName();
        this.glutenFree = recipe.isGlutenFree();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeView that = (RecipeView) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                isGlutenFree() == that.isGlutenFree();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
