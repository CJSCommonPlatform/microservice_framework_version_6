package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.UUID;

import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;

/**
 * Helper class to provide trace string for logging of JsonEnvelopes
 *
 * Deprecated: logic had moved to JsonEnvelope.toString(). Please
 * use that instead.
 */
@Deprecated
public class JsonEnvelopeLoggerHelper {

    public static String toEnvelopeTraceString(final JsonEnvelope envelope) {
        return new JsonEnvelopeLoggerHelper().toTraceString(envelope);
    }

    public String toTraceString(final JsonEnvelope envelope) {
        return envelope.toString();
    }
}
