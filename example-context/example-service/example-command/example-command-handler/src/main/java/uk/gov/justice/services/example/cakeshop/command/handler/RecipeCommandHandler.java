package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonObjects.getBoolean;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.Tolerance;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.example.cakeshop.domain.Ingredient;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;

@ServiceComponent(COMMAND_HANDLER)
public class RecipeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_GLUTEN_FREE = "glutenFree";
    private static final String FIELD_PHOTO_ID = "photoId";

    @Inject
    EventSource eventSource;

    @Inject
    AggregateService aggregateService;

    @Inject
    Enveloper enveloper;

    @Handles("example.add-recipe")
    public void addRecipe(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("=============> Inside add-recipe Command Handler. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();
        final String name = getString(command.payloadAsJsonObject(), FIELD_NAME).get();
        final Boolean glutenFree = getBoolean(command.payloadAsJsonObject(), FIELD_GLUTEN_FREE).get();
        final List<Ingredient> ingredients = ingredientsFrom(command.payloadAsJsonObject());

        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.addRecipe(recipeId, name, glutenFree, ingredients)
                        .map(enveloper.withMetadataFrom(command)));
    }

    @Handles("example.rename-recipe")
    public void renameRecipe(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("=============> Inside rename-recipe Command Handler");

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();
        final String name = getString(command.payloadAsJsonObject(), FIELD_NAME).get();

        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.renameRecipe(name)
                        .map(enveloper.withMetadataFrom(command)),
                Tolerance.NON_CONSECUTIVE);

    }

    @Handles("example.remove-recipe")
    public void removeRecipe(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("=============> Inside remove-recipe Command Handler. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();

        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.removeRecipe()
                        .map(enveloper.withMetadataFrom(command)));
    }

    @Handles("example.upload-photograph")
    public void uploadPhotograph(final JsonEnvelope command) throws EventStreamException {
        LOGGER.trace("=============> Inside upload-photograph Command Handler. RecipeId: " + command.payloadAsJsonObject().getString(FIELD_RECIPE_ID));

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();
        final UUID photoId = getUUID(command.payloadAsJsonObject(), FIELD_PHOTO_ID).get();

        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.addPhotograph(photoId)
                        .map(enveloper.withMetadataFrom(command)),
                Tolerance.NON_CONSECUTIVE);
    }

    private List<Ingredient> ingredientsFrom(final JsonObject payload) {
        return payload.getJsonArray("ingredients").getValuesAs(JsonObject.class).stream()
                .map(jo -> new Ingredient(jo.getString("name"), jo.getInt("quantity")))
                .collect(Collectors.toList());
    }
}


