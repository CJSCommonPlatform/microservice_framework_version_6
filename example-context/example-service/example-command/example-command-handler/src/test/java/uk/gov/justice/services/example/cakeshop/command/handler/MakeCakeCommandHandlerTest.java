package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.EMPTY_LIST;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Tolerance.CONSECUTIVE;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.matchers.EventStreamMatcher.eventStreamAppendedWith;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.CakeMade;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandHandlerTest {

    private static final String COMMAND_NAME = "example.command.make-cake";
    private static final String EVENT_NAME = "example.cake-made";
    private static final UUID CAKE_ID = randomUUID();
    private static final UUID RECIPE_ID = randomUUID();

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private MakeCakeCommandHandler makeCakeCommandHandler;

    @Before
    public void setup() throws Exception {
        createEnveloperWithEvents(CakeMade.class);
    }

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(makeCakeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("makeCake").thatHandles(COMMAND_NAME)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {

        final Recipe recipe = new Recipe();
        final String cakeName = "Chocolate cake";
        recipe.apply(new RecipeAdded(RECIPE_ID, cakeName, false, EMPTY_LIST));

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);

        final JsonEnvelope command = envelopeFrom(
                metadataWithRandomUUID(COMMAND_NAME),
                createObjectBuilder()
                        .add("recipeId", RECIPE_ID.toString())
                        .add("cakeId", CAKE_ID.toString())
                        .build());

        makeCakeCommandHandler.makeCake(command);

        assertThat(eventStream, eventStreamAppendedWith(
                streamContaining(
                        jsonEnvelope(
                                withMetadataEnvelopedFrom(command)
                                        .withName(EVENT_NAME),
                                payloadIsJson(allOf(
                                        withJsonPath("$.cakeId", equalTo(CAKE_ID.toString())),
                                        withJsonPath("$.name", equalTo(cakeName))
                                )))
                                .thatMatchesSchema()
                ))
                .withToleranceOf(CONSECUTIVE));
    }


}
