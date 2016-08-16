package uk.gov.justice.services.example.cakeshop.event.listener;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
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
public class RecipeRemovedEventListenerTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Recipe recipe;

    @Mock
    private JsonObject payload;

    @InjectMocks
    private RecipeRemovedEventListener recipeRemovedEventListener;

    @Before
    public void setup() {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        String recipeId = UUID.randomUUID().toString();
        when(envelope.payloadAsJsonObject().getString(anyString())).thenReturn(recipeId);
        when(recipeRepository.findBy(UUID.fromString(recipeId))).thenReturn(recipe);
    }

    @Test
    public void shouldHandleRecipeRemovedEvent() throws Exception {
        recipeRemovedEventListener.recipeRemoved(envelope);

        verify(recipeRepository).remove(recipe);
    }
}