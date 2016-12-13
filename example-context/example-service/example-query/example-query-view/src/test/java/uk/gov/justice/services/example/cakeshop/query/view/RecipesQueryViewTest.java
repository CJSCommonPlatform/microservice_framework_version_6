package uk.gov.justice.services.example.cakeshop.query.view;


import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.*;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.RecipeService;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonArray;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipesQueryViewTest {

    private RecipesQueryView queryView;

    @Mock
    private RecipeService service;


    @Before
    public void setup() {
        final Enveloper enveloper = createEnveloper();
        queryView = new RecipesQueryView(service, enveloper);
    }

    @Test
    public void shouldHaveCorrectHandlerMethod() throws Exception {
        assertThat(queryView, isHandler(QUERY_VIEW)
                .with(allOf(
                        method("findRecipe").thatHandles("example.get-recipe"),
                        method("listRecipes").thatHandles("example.search-recipes")))
        );
    }

    @Test
    public void shouldReturnRecipe() {

        final UUID recipeId = UUID.randomUUID();
        final String recipeName = "some recipe name";
        when(service.findRecipe(recipeId.toString())).thenReturn(new RecipeView(recipeId, recipeName, false));

        final JsonEnvelope response = queryView.findRecipe(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf(recipeId.toString(), "recipeId").build());

        assertThat(response.payloadAsJsonObject().getString("id"), is(recipeId.toString()));
        assertThat(response.payloadAsJsonObject().getString("name"), is(recipeName));

    }

    @Test
    public void shouldReturnResponseWithMetadataWhenQueryingForRecipe() {

        final JsonEnvelope response = queryView.findRecipe(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf("123", "recipeId").build());

        assertThat(response.metadata().name(), is("example.findRecipe-response"));

    }

    @Test
    public void shouldReturnRecipes() throws Exception {

        final UUID recipeId = UUID.randomUUID();
        final UUID recipeId2 = UUID.randomUUID();
        final String recipeName = "some recipe name";
        final String recipeName2 = "some other recipe name";

        final int pageSize = 5;
        when(service.getRecipes(pageSize, Optional.empty(), Optional.empty()))
                .thenReturn(new RecipesView(asList(new RecipeView(recipeId, recipeName, false), new RecipeView(recipeId2, recipeName2, false))));

        final JsonEnvelope response = queryView.listRecipes(envelope().with(metadataWithDefaults())
                .withPayloadOf(pageSize, "pagesize").build());


        final JsonArray recipesArray = response.payloadAsJsonObject().getJsonArray("recipes");
        assertThat(recipesArray.getJsonObject(0).getString("id"), is(recipeId.toString()));
        assertThat(recipesArray.getJsonObject(0).getString("name"), is(recipeName));

        assertThat(recipesArray.getJsonObject(1).getString("id"), is(recipeId2.toString()));
        assertThat(recipesArray.getJsonObject(1).getString("name"), is(recipeName2));

    }


    @Test
    public void shouldQueryForRecipesOfGivenName() throws Exception {

        final UUID recipeId = UUID.randomUUID();
        final String recipeName = "some recipe name";

        final String nameUsedInQuery = "some recipe";

        final int pagesize = 5;
        when(service.getRecipes(pagesize, Optional.of(nameUsedInQuery), Optional.empty())).thenReturn(new RecipesView(asList(new RecipeView(recipeId, recipeName, false))));

        final JsonEnvelope response = queryView.listRecipes(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf(pagesize, "pagesize")
                        .withPayloadOf(nameUsedInQuery, "name")
                        .build());


        final JsonArray recipesArray = response.payloadAsJsonObject().getJsonArray("recipes");
        assertThat(recipesArray.getJsonObject(0).getString("id"), is(recipeId.toString()));
        assertThat(recipesArray.getJsonObject(0).getString("name"), is(recipeName));

    }

    @Test
    public void shouldQueryForGlutenFreeRecipes() throws Exception {

        final UUID recipeId = UUID.randomUUID();
        final String recipeName = "some recipe name";


        final int pagesize = 5;
        final boolean glutenFree = true;

        when(service.getRecipes(pagesize, Optional.empty(), Optional.of(glutenFree))).thenReturn(
                new RecipesView(asList(new RecipeView(recipeId, recipeName, glutenFree))));

        final JsonEnvelope response = queryView.listRecipes(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf(pagesize, "pagesize")
                        .withPayloadOf(glutenFree, "glutenFree")
                        .build());


        final JsonArray recipesArray = response.payloadAsJsonObject().getJsonArray("recipes");
        assertThat(recipesArray.getJsonObject(0).getString("id"), is(recipeId.toString()));
        assertThat(recipesArray.getJsonObject(0).getString("name"), is(recipeName));
        assertThat(recipesArray.getJsonObject(0).getBoolean("glutenFree"), is(glutenFree));

    }

    @Test
    public void shouldReturnResponseWithMetadataWhenQueryingForRecipes() {

        final JsonEnvelope response = queryView.listRecipes(
                envelope().with(metadataWithDefaults())
                        .withPayloadOf(1, "pagesize").build());

        assertThat(response.metadata().name(), is("example.recipes-response"));

    }


}
