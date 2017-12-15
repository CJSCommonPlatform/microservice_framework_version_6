package uk.gov.justice.services.core.json;

import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;

import org.json.JSONObject;

/**
 * Remove metadata from json envelope as String and return as JSONObject.
 */
public class PayloadExtractor {

    /**
     * Remove metadata if present and return payload part of json envelope as {@link JSONObject}.
     *
     * @param envelopeJson json envelope string to convert
     * @return payload as {@link JSONObject}
     */
    public JSONObject extractPayloadFrom(final String envelopeJson) {

        final JSONObject jsonObject = new JSONObject(envelopeJson);
        jsonObject.remove(METADATA);
        return jsonObject;
    }
}
