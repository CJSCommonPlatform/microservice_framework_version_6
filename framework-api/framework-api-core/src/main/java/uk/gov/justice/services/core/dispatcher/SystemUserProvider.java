package uk.gov.justice.services.core.dispatcher;

import java.util.Optional;
import java.util.UUID;

public interface SystemUserProvider {
    Optional<UUID> getContextSystemUserId();
}