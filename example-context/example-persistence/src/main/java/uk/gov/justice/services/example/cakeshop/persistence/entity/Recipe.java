package uk.gov.justice.services.example.cakeshop.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "recipe")
public class Recipe implements Serializable {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, insertable = true, updatable = true)
    private String name;

    @Column(name = "glutenFree", nullable = false, insertable = true, updatable = true)
    private boolean glutenFree;

    public Recipe(final UUID id, final String name, final boolean glutenFree) {
        this.id = id;
        this.name = name;
        this.glutenFree = glutenFree;
    }

    public Recipe() {

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
        Recipe recipe = (Recipe) o;
        return Objects.equals(getId(), recipe.getId()) &&
                Objects.equals(getName(), recipe.getName()) &&
                isGlutenFree() == recipe.isGlutenFree();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), isGlutenFree());
    }
}