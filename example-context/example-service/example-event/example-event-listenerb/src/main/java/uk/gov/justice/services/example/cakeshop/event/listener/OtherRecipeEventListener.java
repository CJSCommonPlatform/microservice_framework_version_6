package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;

@ServiceComponent(EVENT_LISTENER)
public class OtherRecipeEventListener {

    @Inject
    Logger logger;

    @Handles("other.recipe-added")
    public void recipeAdded(final JsonEnvelope event) {
        logger.info("I received an {} event", event.metadata().name());
    }

    @Handles("other.recipe-renamed")
    public void recipeRenamed(final JsonEnvelope event) {
        logger.info("I received an {} event", event.metadata().name());
    }

    @Handles("other.recipe-removed")
    public void recipeRemoved(final JsonEnvelope event) {
        logger.info("I received an {} event", event.metadata().name());
    }

    @Handles("other.recipe-photograph-added")
    public void recipePhotographAdded(final JsonEnvelope event) {
        logger.info("I received an {} event", event.metadata().name());
    }

}
