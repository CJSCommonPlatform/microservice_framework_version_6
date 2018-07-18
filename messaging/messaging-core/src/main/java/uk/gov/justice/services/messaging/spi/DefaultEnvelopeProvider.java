package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import javax.json.JsonObject;

public class DefaultEnvelopeProvider implements EnvelopeProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Envelope<T> envelopeFrom(final Metadata metadata, final T payload) {
        return new DefaultEnvelope(metadata, payload);
    }

    @Override
    public <T> Envelope<T> envelopeFrom(final MetadataBuilder metadataBuilder, final T payload) {
        return envelopeFrom(metadataBuilder.build(), payload);
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
