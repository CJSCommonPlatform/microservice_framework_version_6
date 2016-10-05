package uk.gov.justice.services.example.cakeshop.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.example.cakeshop.event.listener.converter.RecipeAddedToRecipeConverter;
import uk.gov.justice.services.example.cakeshop.persistence.RecipeRepository;
import uk.gov.justice.services.example.cakeshop.persistence.entity.Recipe;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class RecipeRemovedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeRemovedEventListener.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    RecipeAddedToRecipeConverter recipeAddedToRecipeConverter;

    @Inject
    RecipeRepository recipeRepository;

    @Handles("example.recipe-removed")
    public void recipeRemoved(final JsonEnvelope event) {
        final String recipeId = event.payloadAsJsonObject().getString(FIELD_RECIPE_ID);
        LOGGER.trace("=============> Inside remove-recipe Event Listener about to find recipeId: " + recipeId);
        Recipe recipeFound = recipeRepository.findBy(UUID.fromString(recipeId));
        LOGGER.trace("=============> Found remove-recipe Event Listener. RecipeId: " + recipeFound);
        recipeRepository.remove(recipeFound);
    }
}
