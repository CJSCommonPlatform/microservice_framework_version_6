package uk.gov.justice.services.example.cakeshop.persistence.entity;


import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Access(AccessType.FIELD)
@Table(name = "recipe_ingredients_list")
@SequenceGenerator(name = "RECIPE_INGREDIENT_SEQUENCE", sequenceName = "recipe_ingredient_sequence"/*, allocationSize = 1, initialValue = 0*/)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECIPE_INGREDIENT_SEQUENCE")
    private Long id;

    @Column(name = "recipe_id", nullable = false, insertable = true, updatable = true)
    private UUID recipeId;

    @Column(name = "ingredient_name", nullable = false, insertable = true, updatable = true)
    private String ingredientName;

    @Column(name = "quantity", nullable = true, insertable = true, updatable = true)
    private Integer quantity;

    public RecipeIngredient() {

    }

    public RecipeIngredient(final Long id, final UUID recipeId, final String ingredientName, final Integer quantity) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public UUID getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(final UUID recipeId) {
        this.recipeId = recipeId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(final String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}
