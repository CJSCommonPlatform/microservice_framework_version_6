package uk.gov.justice.services.messaging;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Interface for a messaging envelope containing metadata and a JsonValue payload.
 */
public interface JsonEnvelope extends Envelope<JsonValue> {

    String METADATA = "_metadata";

    JsonObject payloadAsJsonObject();

    JsonArray payloadAsJsonArray();

    JsonNumber payloadAsJsonNumber();

    JsonString payloadAsJsonString();

}
