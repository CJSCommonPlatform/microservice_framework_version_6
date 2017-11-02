package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class DummyJsonEnvelopeProvider extends JsonEnvelopeProvider {

    @Override
    public JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return null;
    }

    @Override
    public JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonValue payload) {
        return null;
    }

    @Override
    public JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonObjectBuilder payloadBuilder) {
        return null;
    }

    @Override
    public MetadataBuilder metadataBuilder() {
        return null;
    }

    @Override
    public MetadataBuilder metadataFrom(final Metadata metadata) {
        return null;
    }

    @Override
    public MetadataBuilder metadataFrom(final JsonObject jsonObject) {
        return null;
    }
}
