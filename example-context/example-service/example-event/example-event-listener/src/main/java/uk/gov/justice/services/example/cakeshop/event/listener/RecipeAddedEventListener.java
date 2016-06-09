package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class RecipeAddedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeAddedEventListener.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    RecipeAddedToRecipeConverter recipeAddedConverter;

    @Inject
    RecipeRepository recipeRepository;

    @Handles("cakeshop.recipe-added")
    public void recipeAdded(final JsonEnvelope event) {

        String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        LOGGER.info("=============> Inside add-recipe Event Listener. RecipeId: " + recipeId);

        recipeRepository.save(
                recipeAddedConverter.convert(
                        jsonObjectConverter.convert(
                                event.payloadAsJsonObject(),
                                RecipeAdded.class)));
        LOGGER.info("=====================================================> Recipe saved, RecipeId: " + recipeId);
    }
}
