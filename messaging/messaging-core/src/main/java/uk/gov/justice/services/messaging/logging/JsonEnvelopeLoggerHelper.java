package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.messaging.JsonEnvelope;

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
