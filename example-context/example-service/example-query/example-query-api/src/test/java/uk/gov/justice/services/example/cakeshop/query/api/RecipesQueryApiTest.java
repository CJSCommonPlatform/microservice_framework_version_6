package uk.gov.justice.services.example.cakeshop.query.api;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RecipesQueryApiTest {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    Envelope envelope;

    @Mock
    Envelope response;

    @Mock
    private Requester requester;

    @Mock
    JsonObject payload;

    @InjectMocks
    private RecipesQueryApi recipesQueryApi;

    @Test
    public void shouldHandleRecipesQuery() throws Exception {
        when(envelope.payload()).thenReturn(payload);
        when(requester.request(envelope)).thenReturn(response);

        Envelope actualResponse = recipesQueryApi.listRecipes(envelope);

        verify(requester, times(1)).request(envelope);
        assertThat(actualResponse, sameInstance(response));
    }

    @Test
    public void shouldHandleRecipeQuery() throws Exception {
        when(envelope.payload()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());
        when(requester.request(envelope)).thenReturn(response);

        Envelope actualResponse = recipesQueryApi.recipe(envelope);

        verify(payload, times(1)).getString(FIELD_RECIPE_ID);
        verify(requester, times(1)).request(envelope);
        assertThat(actualResponse, sameInstance(response));
    }
}