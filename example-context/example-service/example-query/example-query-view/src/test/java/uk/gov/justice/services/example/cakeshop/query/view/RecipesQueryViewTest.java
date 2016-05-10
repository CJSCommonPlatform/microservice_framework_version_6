package uk.gov.justice.services.example.cakeshop.query.view;

import static javax.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.enveloper.Enveloper;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipeView;
import uk.gov.justice.services.example.cakeshop.query.view.response.RecipesView;
import uk.gov.justice.services.example.cakeshop.query.view.service.RecipeService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipesQueryViewTest {

    public static final String ID = UUID.randomUUID().toString();
    public static final String NAME = "Cake";
    static final String NAME_RESPONSE_RECIPE = "cakeshop.query.findRecipe-response";
    static final String NAME_RESPONSE_RECIPE_LIST = "cakeshop.query.recipes-response";
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_NAME = "name";
    @InjectMocks
    private RecipesQueryView queryView;

    @Mock
    private RecipeService service;

    @Mock
    private Enveloper enveloper;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private JsonObject payload;

    @Mock
    private RecipeView recipeView;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelope updatedEnvelope;

    @Mock
    private JsonEnvelope envelopeWithNullPayload;

    @Before
    public void setup() {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(payload.isEmpty()).thenReturn(false);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(ID);
        when(payload.getString(FIELD_NAME)).thenReturn(NAME);
    }

    @Test
    public void shouldReturnRecipeView() {
        when(service.findRecipe(ID)).thenReturn(recipeView);
        when(enveloper.withMetadataFrom(envelope, NAME_RESPONSE_RECIPE)).thenReturn(function);
        when(function.apply(recipeView)).thenReturn(updatedEnvelope);

        JsonEnvelope actualEnvelope = queryView.findRecipe(envelope);

        assertThat(updatedEnvelope, equalTo(actualEnvelope));
    }

    @Test
    public void shouldReturnEnevlopeWithNullPayload() {
        when(service.findRecipe(ID)).thenReturn(null);
        when(enveloper.withMetadataFrom(envelope, NAME_RESPONSE_RECIPE)).thenReturn(function);
        when(function.apply(null)).thenReturn(envelopeWithNullPayload);
        when(envelopeWithNullPayload.payload()).thenReturn(NULL);

        JsonEnvelope actualEnvelope = queryView.findRecipe(envelope);

        assertThat(actualEnvelope, equalTo(envelopeWithNullPayload));
        assertThat(actualEnvelope.payload(), equalTo(NULL));
    }

    @Test
    public void shouldReturnRecipeViewByName() {
        RecipesView recipes = new RecipesView(Collections.singletonList(recipeView));
        when(service.findByName(NAME)).thenReturn(recipes);
        when(enveloper.withMetadataFrom(envelope, NAME_RESPONSE_RECIPE_LIST)).thenReturn(function);
        when(function.apply(recipes)).thenReturn(updatedEnvelope);

        JsonEnvelope actualEnvelope = queryView.listRecipes(envelope);

        assertThat(actualEnvelope, equalTo(updatedEnvelope));
    }

    @Test
    public void shouldReturnAllRecipes() {
        when(payload.getString(FIELD_NAME)).thenReturn(null);
        when(payload.isEmpty()).thenReturn(true);
        RecipesView recipes = new RecipesView(Collections.singletonList(recipeView));
        when(service.getRecipes()).thenReturn(recipes);
        when(enveloper.withMetadataFrom(envelope, NAME_RESPONSE_RECIPE_LIST)).thenReturn(function);
        when(function.apply(recipes)).thenReturn(updatedEnvelope);

        JsonEnvelope actualEnvelope = queryView.listRecipes(envelope);

        assertThat(actualEnvelope, equalTo(updatedEnvelope));
    }

}
