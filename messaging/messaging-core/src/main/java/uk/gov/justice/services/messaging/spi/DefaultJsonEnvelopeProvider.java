package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class DefaultJsonEnvelopeProvider extends JsonEnvelopeProvider {

    @Override
    public JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return new DefaultJsonEnvelope(metadata, payload);
    }

    @Override
    public JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonValue payload) {
        return envelopeFrom(metadataBuilder.build(), payload);
    }

    @Override
    public JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonObjectBuilder payloadBuilder) {
        return envelopeFrom(metadataBuilder.build(), payloadBuilder.build());
    }

    @Override
    public MetadataBuilder metadataBuilder() {
        return DefaultJsonMetadata.metadataBuilder();
    }

    @Override
    public MetadataBuilder metadataFrom(final Metadata metadata) {
        return DefaultJsonMetadata.metadataBuilderFrom(metadata);
    }

    @Override
    public MetadataBuilder metadataFrom(final JsonObject jsonObject) {
        return DefaultJsonMetadata.metadataBuilderFrom(jsonObject);
    }
}
