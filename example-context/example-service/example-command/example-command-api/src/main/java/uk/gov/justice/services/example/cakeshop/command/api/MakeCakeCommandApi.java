package uk.gov.justice.services.example.cakeshop.command.api;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class MakeCakeCommandApi {

    @Inject
    Sender sender;

    @Inject
    Enveloper enveloper;

    @Handles("example.make-cake")
    public JsonEnvelope handle(final JsonEnvelope envelope) {
        sender.send(enveloper
                .withMetadataFrom(envelope, "example.command.make-cake")
                .apply(envelope.payloadAsJsonObject()));

        return envelopeFrom(
                metadataFrom(envelope.metadata()),
                createObjectBuilder().add("status", "Making Cake"));
    }
}
