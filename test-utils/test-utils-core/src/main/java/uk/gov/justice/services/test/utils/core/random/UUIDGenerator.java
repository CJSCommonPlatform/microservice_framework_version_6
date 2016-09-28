package uk.gov.justice.services.test.utils.core.random;

import java.util.UUID;

public class UUIDGenerator extends Generator<UUID> {

    @Override
    public UUID next() {
        return UUID.randomUUID();
    }
}
