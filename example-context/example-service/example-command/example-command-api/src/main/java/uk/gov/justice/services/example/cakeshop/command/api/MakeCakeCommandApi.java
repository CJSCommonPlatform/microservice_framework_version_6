package uk.gov.justice.services.example.cakeshop.command.api;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_API)
public class MakeCakeCommandApi {

    @Inject
    Sender sender;

    @Handles("example.make-cake")
    public Envelope<JsonObject> handle(final JsonEnvelope envelope) {
        sender.send(envelop(envelope.payloadAsJsonObject())
                .withName("example.command.make-cake")
                .withMetadataFrom(envelope));

        return envelop(
                createObjectBuilder()
                        .add("status", "Making Cake")
                        .build())
                .withName("example.command.make-cake-status")
                .withMetadataFrom(envelope);
    }
}
