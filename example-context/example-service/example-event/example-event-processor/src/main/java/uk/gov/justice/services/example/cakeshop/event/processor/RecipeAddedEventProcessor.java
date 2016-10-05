package uk.gov.justice.services.example.cakeshop.event.processor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class RecipeAddedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeAddedEventProcessor.class);

    @Inject
    Sender sender;

    @Handles("example.recipe-added")
    public void recipeAdded(final JsonEnvelope event) {
        LOGGER.info("=============> Inside recipe-added Event Processor");
        sender.send(event);

    }
}
