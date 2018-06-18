package uk.gov.justice.services.example.cakeshop.it.helpers;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.ADD_RECIPE_MEDIA_TYPE;
import static uk.gov.justice.services.example.cakeshop.it.params.CakeShopMediaTypes.RENAME_RECIPE_MEDIA_TYPE;

import javax.ws.rs.client.Entity;

public class EventFactory {

    public Entity<String> renameRecipeEntity(final String recipeName) {
        return entity(
                createObjectBuilder()
                        .add("name", recipeName)
                        .build().toString(),
                RENAME_RECIPE_MEDIA_TYPE);
    }

    public Entity<String> recipeEntity(String recipeName, boolean glutenFree) {
        return entity(
                createObjectBuilder()
                        .add("name", recipeName)
                        .add("glutenFree", glutenFree)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "someIngredient")
                                        .add("quantity", 1)
                                ).build()
                        ).build().toString(),
                ADD_RECIPE_MEDIA_TYPE);
    }

    public Entity<String> recipeEntity(final String recipeName) {
        final boolean glutenFree = false;
        return recipeEntity(recipeName, glutenFree);
    }
}
