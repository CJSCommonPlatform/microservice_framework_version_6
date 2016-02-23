package uk.gov.justice.services.messaging;

import javax.json.JsonObject;

/**
 * Interface for a messaging envelope containing metadata and a payload.
 */
public interface Envelope {

    public static final String METADATA = "metadata";
    public static final String PAYLOAD = "payload";

    Metadata metadata();

    JsonObject payload();
}
