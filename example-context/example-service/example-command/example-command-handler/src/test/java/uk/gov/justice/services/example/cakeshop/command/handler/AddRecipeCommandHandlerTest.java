package uk.gov.justice.services.example.cakeshop.command.handler;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.events.recipe-added";
    private static final UUID RECIPE_ID = UUID.randomUUID();
    private static final String RECIPE_NAME = "Test Recipe";

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
        final JsonEnvelope command = createCommand();

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);
        when(recipe.addRecipe(RECIPE_ID, RECIPE_NAME, emptyList())).thenReturn(Stream.of(event));
        when(enveloper.withMetadataFrom(command)).thenReturn(x -> x.equals(event) ? envelope : null);

        addRecipeCommandHandler.addRecipe(command);

        verify(eventStream).append(streamCaptor.capture());
        assertThat(streamCaptor.getValue().collect(toList()), hasItems(envelope));
    }

    private JsonEnvelope createCommand() {
        final JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, EVENT_NAME)
                .build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("recipeId", RECIPE_ID.toString())
                .add("name", RECIPE_NAME)
                .add("ingredients", Json.createArrayBuilder().build())
                .build();

        return envelopeFrom(metadataFrom(metadataAsJsonObject), payloadAsJsonObject);
    }
}
