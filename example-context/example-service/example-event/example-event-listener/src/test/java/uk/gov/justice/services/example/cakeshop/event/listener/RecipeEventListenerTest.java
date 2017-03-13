package uk.gov.justice.services.example.cakeshop.event.listener;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToIngredientsConverter;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.IngredientRepository;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Ingredient;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeEventListenerTest {

    private static final String INGREDIENT_NAME = "Flour";

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private RecipeAddedToRecipeConverter recipeAddedToRecipeConverter;

    @Mock
    private RecipeAddedToIngredientsConverter recipeAddedToIngredientsConverter;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private RecipeAdded recipeAdded;

    @Mock
    private Recipe recipe;

    @Mock
    private Ingredient ingredient;

    @Mock
    private JsonObject payload;

    @InjectMocks
    private RecipeEventListener recipeEventListener;

    @Before
    public void setup() {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
    }

    @Test
    public void shouldHandleRecipeAddedEvent() throws Exception {
        when(recipeAddedToRecipeConverter.convert(recipeAdded)).thenReturn(recipe);
        when(ingredient.getName()).thenReturn(INGREDIENT_NAME);
        when(recipeAddedToIngredientsConverter.convert(recipeAdded)).thenReturn(singletonList(ingredient));
        when(ingredientRepository.findByNameIgnoreCase(INGREDIENT_NAME)).thenReturn(emptyList());
        when(jsonObjectToObjectConverter.convert(payload, RecipeAdded.class)).thenReturn(recipeAdded);

        recipeEventListener.recipeAdded(envelope);

        verify(recipeRepository).save(recipe);
        verify(ingredientRepository).save(ingredient);
    }

    @Test
    public void shouldHandleRecipeAddedEventWithExistingIngredient() throws Exception {
        when(recipeAddedToRecipeConverter.convert(recipeAdded)).thenReturn(recipe);
        when(ingredient.getName()).thenReturn(INGREDIENT_NAME);
        when(recipeAddedToIngredientsConverter.convert(recipeAdded)).thenReturn(singletonList(ingredient));
        when(ingredientRepository.findByNameIgnoreCase(INGREDIENT_NAME)).thenReturn(singletonList(ingredient));
        when(jsonObjectToObjectConverter.convert(payload, RecipeAdded.class)).thenReturn(recipeAdded);

        recipeEventListener.recipeAdded(envelope);

        verify(recipeRepository).save(recipe);
        verify(ingredientRepository, never()).save(ingredient);
    }

    @Test
    public void shouldHandleRecipeRenamedEvent() throws Exception {
        final String recipeId = randomUUID().toString();
        final String name = "recipe name";

        when(envelope.payloadAsJsonObject().getString("recipeId")).thenReturn(recipeId);
        when(envelope.payloadAsJsonObject().getString("name")).thenReturn(name);
        when(recipeRepository.findBy(UUID.fromString(recipeId))).thenReturn(recipe);

        recipeEventListener.recipeRenamed(envelope);

        verify(recipe).setName(name);
        verify(recipeRepository).save(recipe);
    }

    @Test
    public void shouldHandleRecipeRemovedEvent() throws Exception {
        final String recipeId = randomUUID().toString();

        when(envelope.payloadAsJsonObject().getString(anyString())).thenReturn(recipeId);
        when(recipeRepository.findBy(UUID.fromString(recipeId))).thenReturn(recipe);

        recipeEventListener.recipeRemoved(envelope);

        verify(recipeRepository).remove(recipe);
    }

    @Test
    public void shouldHandlePhotographAddedEvent() throws Exception {
        final UUID recipeId = randomUUID();
        final UUID photoId = randomUUID();

        when(envelope.payloadAsJsonObject().getString("recipeId")).thenReturn(recipeId.toString());
        when(envelope.payloadAsJsonObject().getString("photoId")).thenReturn(photoId.toString());
        when(recipeRepository.findBy(UUID.fromString(recipeId.toString()))).thenReturn(recipe);

        recipeEventListener.recipePhotographAdded(envelope);

        verify(recipe).setPhotoId(photoId);
        verify(recipeRepository).save(recipe);
    }
}