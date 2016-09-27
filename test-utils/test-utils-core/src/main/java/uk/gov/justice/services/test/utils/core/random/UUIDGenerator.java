package uk.gov.justice.services.test.utils.core.random;

import java.util.UUID;

public class UUIDGenerator implements Generator<UUID> {

    public UUID next() {
        return UUID.randomUUID();
    }
}
