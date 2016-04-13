package uk.gov.justice.services.example.cakeshop.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class RecipeAddedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeAddedEventListener.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Handles("cakeshop.events.recipe-added")
    public void handle(final JsonEnvelope envelope) {

        LOGGER.info("=============> Inside add-recipe Event Listener. RecipeId: " + envelope.payloadAsJsonObject().getString(FIELD_RECIPE_ID));
        LOGGER.info("===============================================================================================");

    }

}
