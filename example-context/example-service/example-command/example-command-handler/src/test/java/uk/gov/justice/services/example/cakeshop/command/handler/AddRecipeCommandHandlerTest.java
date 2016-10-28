package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.EventStreamMatcher.eventStreamAppendedWith;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

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

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(RecipeAdded.class);

    @InjectMocks
    private AddRecipeCommandHandler addRecipeCommandHandler;

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(addRecipeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("addRecipe").thatHandles("example.add-recipe")));
    }

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        final Recipe recipe = new Recipe();
        final UUID commandId = UUID.randomUUID();

        final JsonEnvelope command = envelopeFrom(
                metadataOf(commandId, COMMAND_NAME),
                createObjectBuilder()
                        .add("recipeId", RECIPE_ID.toString())
                        .add("name", RECIPE_NAME)
                        .add("glutenFree", GULTEN_FREE)
                        .add("ingredients", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("name", "Flour")
                                        .add("quantity", 200)))
                        .build());

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);

        addRecipeCommandHandler.addRecipe(command);

        assertThat(eventStream, eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.recipeId", equalTo(RECIPE_ID.toString())),
                                        withJsonPath("$.name", equalTo(RECIPE_NAME)),
                                        withJsonPath("$.glutenFree", equalTo(GULTEN_FREE)),
                                        withJsonPath("$.ingredients.length()", equalTo(1)),
                                        withJsonPath("$.ingredients[0].name", equalTo("Flour")),
                                        withJsonPath("$.ingredients[0].quantity", equalTo(200))
                                )))
                                .thatMatchesSchema()
                )));
    }
}
