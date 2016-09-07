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

    /**
     * Pretty prints the actual json of this envelope.
     *
     * N.B. This should NEVER be used in production code as the json
     * may contain sensitive data which can be harmful should it appear
     * in logs.
     *
     * If you want to log this envelope for tracing purposes
     * then use the toString() method, as this will give the
     * objects metadata without exposing anything sensistive
     *
     * @return this envelope as it's json string, pretty printed.
     **/
    String toDebugStringPrettyPrint();


    /**
     * Prints the json for logging purposes. Removes any potentially sensitive
     * data.
     *
     * @return a json String of the envelope
     */
    String toString();
}
