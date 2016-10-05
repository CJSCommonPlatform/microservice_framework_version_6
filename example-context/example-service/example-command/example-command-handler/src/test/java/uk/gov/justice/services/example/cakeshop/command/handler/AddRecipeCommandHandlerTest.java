package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payLoad;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandHandlerTest {

    private static final String COMMAND_NAME = "example.add-recipe";
    private static final String EVENT_NAME = "example.recipe-added";
    private static final UUID RECIPE_ID = UUID.randomUUID();
    private static final String RECIPE_NAME = "Test Recipe";
    private static final Boolean GULTEN_FREE = true;

    @Mock
    EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Recipe recipe;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(RecipeAdded.class);

    @InjectMocks
    private AddRecipeCommandHandler addRecipeCommandHandler;

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        final UUID commandId = UUID.randomUUID();
        final RecipeAdded recipeAdded = new RecipeAdded(RECIPE_ID, RECIPE_NAME, GULTEN_FREE, emptyList());
        final JsonEnvelope command = envelope()
                .with(metadataOf(commandId, COMMAND_NAME))
                .withPayloadOf(RECIPE_ID.toString(), "recipeId")
                .withPayloadOf(RECIPE_NAME, "name")
                .withPayloadOf(GULTEN_FREE, "glutenFree")
                .withPayloadOf(new String[]{}, "ingredients")
                .build();

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);
        when(recipe.addRecipe(RECIPE_ID, RECIPE_NAME, GULTEN_FREE, emptyList())).thenReturn(Stream.of(recipeAdded));

        addRecipeCommandHandler.addRecipe(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        metadata()
                                .withCausationIds(commandId)
                                .withName(EVENT_NAME),
                        payLoad().isJson(allOf(
                                withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                withJsonPath("$.name", equalTo(RECIPE_NAME)),
                                withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
                                withJsonPath("$.ingredients", empty())
                        ))))
        );
    }
}
