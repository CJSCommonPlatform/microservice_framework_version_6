package uk.gov.justice.services.messaging;

import javax.json.JsonValue;

/**
 * Interface for a messaging envelope containing metadata and a payload.
 */
public interface Envelope<T> {

    Metadata metadata();

    T payload();

}
