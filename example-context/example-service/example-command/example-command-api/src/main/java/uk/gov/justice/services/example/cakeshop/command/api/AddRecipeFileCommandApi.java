package uk.gov.justice.services.example.cakeshop.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.file.api.sender.FileSender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_API)
public class AddRecipeFileCommandApi {

    @Inject
    FileSender fileSender;

    @Handles("example.add-recipe-file")
    public void addRecipeFile(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        fileSender.send(payload.getString("fileName"), payload.getString("fileContent").getBytes());
    }
}