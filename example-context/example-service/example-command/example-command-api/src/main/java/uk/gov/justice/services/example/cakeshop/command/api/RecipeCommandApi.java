package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class RecipeCommandApi {

    @Inject
    Sender sender;

    @Inject
    Enveloper enveloper;

    @Handles("example.add-recipe")
    public void addRecipe(final JsonEnvelope envelope) {
        sender.send(enveloper.withMetadataFrom(envelope, "example.command.add-recipe").apply(envelope.payload()));
    }

    @Handles("example.rename-recipe")
    public void renameRecipe(final JsonEnvelope envelope) {
        sender.send(enveloper.withMetadataFrom(envelope, "example.command.rename-recipe").apply(envelope.payload()));
    }

    @Handles("example.remove-recipe")
    public void removeRecipe(final JsonEnvelope envelope) {
        sender.send(enveloper.withMetadataFrom(envelope, "example.command.remove-recipe").apply(envelope.payload()));
    }

    @Handles("example.upload-photograph")
    public void uploadPhotograph(final JsonEnvelope envelope) {
        sender.send(enveloper.withMetadataFrom(envelope, "example.command.upload-photograph").apply(envelope.payload()));
    }
}
