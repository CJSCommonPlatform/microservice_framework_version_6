package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

public class JsonEnvelopeConverter {

    public JsonEnvelope toJsonEnvelope(final Envelope<JsonValue> envelope) {

        if(envelope == null) {
            return null;
        }

        if(JsonEnvelope.class.isAssignableFrom(envelope.getClass())) {
            return (JsonEnvelope) envelope;
        }
        return JsonEnvelope.envelopeFrom(envelope.metadata(), envelope.payload());
    }
}
