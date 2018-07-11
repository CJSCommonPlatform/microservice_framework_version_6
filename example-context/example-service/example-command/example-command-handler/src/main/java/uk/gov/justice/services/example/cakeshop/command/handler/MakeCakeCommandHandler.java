package uk.gov.justice.services.example.cakeshop.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.Tolerance;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;

@ServiceComponent(COMMAND_HANDLER)
public class MakeCakeCommandHandler {

    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_CAKE_ID = "cakeId";

    @Inject
    EventSource eventSource;

    @Inject
    AggregateService aggregateService;

    @Inject
    Enveloper enveloper;

    @Handles("example.command.make-cake")
    public void makeCake(final JsonEnvelope command) throws EventStreamException {

        final UUID recipeId = getUUID(command.payloadAsJsonObject(), FIELD_RECIPE_ID).get();
        final UUID cakeId = getUUID(command.payloadAsJsonObject(), FIELD_CAKE_ID).get();


        final EventStream eventStream = eventSource.getStreamById(recipeId);
        final Recipe recipe = aggregateService.get(eventStream, Recipe.class);

        eventStream.append(
                recipe.makeCake(cakeId)
                        .map(enveloper.withMetadataFrom(command)),
                Tolerance.CONSECUTIVE);

    }

}
