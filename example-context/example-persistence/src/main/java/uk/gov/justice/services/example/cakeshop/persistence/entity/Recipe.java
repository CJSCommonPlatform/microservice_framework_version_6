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

    public Recipe(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Recipe() {

    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(getId(), recipe.getId()) &&
                Objects.equals(getName(), recipe.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}