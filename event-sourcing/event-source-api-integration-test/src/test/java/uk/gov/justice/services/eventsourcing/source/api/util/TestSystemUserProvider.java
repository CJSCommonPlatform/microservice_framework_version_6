package uk.gov.justice.services.eventsourcing.source.api.util;

import static java.util.UUID.randomUUID;

import uk.gov.justice.services.core.dispatcher.SystemUserProvider;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestSystemUserProvider implements SystemUserProvider {
    public static final UUID SYSTEM_USER_ID = randomUUID();

    @Override
    public Optional<UUID> getContextSystemUserId() {
        return Optional.of(SYSTEM_USER_ID);
    }
}