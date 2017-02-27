package uk.gov.justice.services.adapter.rest.interceptor;

import uk.gov.justice.services.messaging.JsonEnvelope;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonEnvelopeTestUtils {

    private static final String METADATA = "_metadata";

    public static String toDebugStringPrettyPrint(final JsonEnvelope jsonEnvelope) {
        return new JSONObject(new JSONTokener(jsonEnvelope.payloadAsJsonObject().toString()))
                .put(METADATA, new JSONObject(jsonEnvelope.metadata().asJsonObject().toString()))
                .toString(2);
    }
}
