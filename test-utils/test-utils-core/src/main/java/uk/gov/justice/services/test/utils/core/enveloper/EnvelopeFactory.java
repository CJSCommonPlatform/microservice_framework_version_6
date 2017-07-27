package uk.gov.justice.services.test.utils.core.enveloper;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;

/**
 * Class for creating a simple JsonEnvelope. Uses a random UUID in the metadata
 */
public class EnvelopeFactory {

    public static final JsonEnvelope createEnvelope(final String commandName, final JsonObject payload) {
        return new EnvelopeFactory().create(commandName, payload);
    }

    public JsonEnvelope create(final String commandName, final JsonObject payload) {
        return envelopeFrom(
                metadataWithRandomUUID(commandName)
                        .build(),
                payload);
    }
}
