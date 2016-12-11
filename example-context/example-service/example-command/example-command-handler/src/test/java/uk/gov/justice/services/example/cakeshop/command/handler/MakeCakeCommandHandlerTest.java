package uk.gov.justice.services.example.cakeshop.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.EMPTY_LIST;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
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
import uk.gov.justice.services.example.cakeshop.domain.event.CakeMade;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collections;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandHandlerTest {

    private static final String COMMAND_NAME = "example.make-cake";
    private static final String EVENT_NAME = "example.cake-made";
    private static final UUID CAKE_ID = randomUUID();
    private static final UUID RECIPE_ID = randomUUID();


    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(CakeMade.class);

    @InjectMocks
    private MakeCakeCommandHandler makeCakeCommandHandler;

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(makeCakeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("makeCake").thatHandles(COMMAND_NAME)));
    }

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {

        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        final Recipe recipe = new Recipe();
        final String cakeName = "Chocolate cake";
        recipe.apply(new RecipeAdded(RECIPE_ID, cakeName, false, EMPTY_LIST));

        when(aggregateService.get(eventStream, Recipe.class)).thenReturn(recipe);

        final JsonEnvelope command = envelope()
                .with(metadataWithRandomUUID(COMMAND_NAME))
                .withPayloadOf(CAKE_ID, "cakeId")
                .withPayloadOf(RECIPE_ID, "recipeId")
                .build();
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
                )));
    }


}
