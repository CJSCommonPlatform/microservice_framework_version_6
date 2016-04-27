package uk.gov.justice.services.example.cakeshop.command.handler;

import org.slf4j.Logger;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.event.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.eventsourcing.source.core.Events.streamOf;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonArray;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

@ServiceComponent(COMMAND_HANDLER)
public class AddRecipeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_NAME = "name";

    @Inject
    EventSource eventSource;

    @Inject
    AggregateService aggregateService;

    @Inject
    Enveloper enveloper;

    @Handles("cakeshop.command.add-recipe")
    public void addRecipe(final JsonEnvelope command) throws EventStreamException {

        LOGGER.info("=============> Inside add-recipe Command Handler. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();
        final String name = getString(command.payloadAsJsonObject(), FIELD_NAME).get();
        final List<Ingredient> ingredients = ingredientsFrom(command.payloadAsJsonObject());

        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.addRecipe(recipeId, name, ingredients)
                        .map(enveloper.withMetadataFrom(command)));
    }

    private List<Ingredient> ingredientsFrom(final JsonObject payload) {
        return payload.getJsonArray("ingredients").getValuesAs(JsonObject.class).stream()
                .map(jo -> new Ingredient(jo.getString("name"), jo.getInt("quantity")))
                .collect(Collectors.toList());
    }

}


