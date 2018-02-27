package uk.gov.justice.services.example.cakeshop.command.handler;

public class RenameRecipe {

    private String recipeId;
    private String name;

    public RenameRecipe(final String recipeId, final String name) {
        this.recipeId = recipeId;
        this.name = name;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(final String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
