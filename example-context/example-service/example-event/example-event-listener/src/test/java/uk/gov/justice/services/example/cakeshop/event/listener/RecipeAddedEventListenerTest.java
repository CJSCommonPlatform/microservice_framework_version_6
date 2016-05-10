package uk.gov.justice.services.example.cakeshop.event.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipeAddedEventListenerTest {

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private RecipeAddedToRecipeConverter recipeAddedToRecipeConverter;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private RecipeAdded recipeAdded;

    @Mock
    private Recipe recipe;

    @Mock
    private JsonObject payload;

    @InjectMocks
    private RecipeAddedEventListener recipeAddedEventListener;

    @Test
    public void shouldHandlePersonRegisteredEvent() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(jsonObjectToObjectConverter.convert(payload, RecipeAdded.class)).thenReturn(recipeAdded);
        when(recipeAddedToRecipeConverter.convert(recipeAdded)).thenReturn(recipe);

        recipeAddedEventListener.recipeAdded(envelope);

        verify(recipeRepository).save(recipe);
    }
}