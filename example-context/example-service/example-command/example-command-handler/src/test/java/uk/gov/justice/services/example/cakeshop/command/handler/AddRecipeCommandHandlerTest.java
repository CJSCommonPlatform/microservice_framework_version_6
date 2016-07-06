package uk.gov.justice.services.example.cakeshop.command.handler;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.recipe-added";
    private static final UUID RECIPE_ID = UUID.randomUUID();
    private static final String RECIPE_NAME = "Test Recipe";
    private static final Boolean GULTEN_FREE = true;

    @Mock
    JsonEnvelope envelope;

    @Mock
    EventSource eventSource;

    @Mock
    EventStream eventStream;

    @Mock
    AggregateService aggregateService;

    @Mock
    Enveloper enveloper;

    @Mock
    Recipe recipe;

    @Mock
    RecipeAdded event;

    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> streamCaptor;

    @InjectMocks
    private AddRecipeCommandHandler addRecipeCommandHandler;

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        final JsonEnvelope command = envelope()
                .with(metadataWithRandomUUID(EVENT_NAME))
                .withPayloadOf(RECIPE_ID.toString(), "recipeId")
                .withPayloadOf(RECIPE_NAME, "name")
                .withPayloadOf(GULTEN_FREE, "glutenFree")
                .withPayloadOf(new String[]{}, "ingredients")
                .build();

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);
        when(recipe.addRecipe(RECIPE_ID, RECIPE_NAME, GULTEN_FREE, emptyList())).thenReturn(Stream.of(event));
        when(enveloper.withMetadataFrom(command)).thenReturn(x -> x.equals(event) ? envelope : null);

        addRecipeCommandHandler.addRecipe(command);

        verify(eventStream).append(streamCaptor.capture());
        assertThat(streamCaptor.getValue().collect(toList()), hasItems(envelope));
    }

}
