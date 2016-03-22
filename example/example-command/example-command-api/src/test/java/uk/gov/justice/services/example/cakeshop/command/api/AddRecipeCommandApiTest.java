package uk.gov.justice.services.example.cakeshop.command.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.JsonObject;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandApiTest {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    Envelope envelope;

    @Mock
    private Sender sender;

    @Mock
    JsonObject payload;

    @InjectMocks
    private AddRecipeCommandApi addRecipeCommandApi;

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        when(envelope.payload()).thenReturn(payload);
        when(payload.getString(FIELD_RECIPE_ID)).thenReturn(RECIPE_ID.toString());

        addRecipeCommandApi.addRecipe(envelope);

        verify(sender, times(1)).send(envelope);
    }

}