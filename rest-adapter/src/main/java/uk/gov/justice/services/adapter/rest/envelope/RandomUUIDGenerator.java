package uk.gov.justice.services.adapter.rest.envelope;

import java.util.UUID;

/**
 * Factory for creating new UUIDs.
 */
public class RandomUUIDGenerator {

    /**
     * Generate a random UUID.
     * @return a UUID
     */
    public UUID generate() {
        return UUID.randomUUID();
    }
}
