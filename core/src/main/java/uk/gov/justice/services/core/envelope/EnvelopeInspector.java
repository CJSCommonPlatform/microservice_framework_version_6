package uk.gov.justice.services.core.envelope;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

public class EnvelopeInspector {

    public String getActionNameFor(final JsonEnvelope jsonEnvelope) {
        return getMetadataFor(jsonEnvelope).name();
    }

    public Metadata getMetadataFor(final JsonEnvelope jsonEnvelope) {
        final Metadata metadata = jsonEnvelope.metadata();
        if (metadata != null) {
            return metadata;
        }

        throw new EnvelopeValidationException("Metadata not set in the envelope.");
    }
}
