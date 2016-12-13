package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(COMMAND_HANDLER)
public class RemoveRecipeCommandHandler {

    private static final Logger LOGGER = getLogger(MakeCakeCommandHandler.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    EventSource eventSource;

    @Inject
    AggregateService aggregateService;

    @Inject
    Enveloper enveloper;

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


}


