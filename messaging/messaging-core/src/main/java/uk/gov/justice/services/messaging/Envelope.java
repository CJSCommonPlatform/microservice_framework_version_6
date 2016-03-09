package uk.gov.justice.services.messaging;

import javax.json.JsonObject;

/**
 * Interface for a messaging envelope containing metadata and a payload.
 */
public interface Envelope {

    Metadata metadata();

    JsonObject payload();

}
