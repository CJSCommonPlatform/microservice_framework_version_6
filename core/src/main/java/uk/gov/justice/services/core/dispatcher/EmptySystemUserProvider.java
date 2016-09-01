package uk.gov.justice.services.core.dispatcher;


import static java.util.Optional.empty;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Empty implementation of the SystemUserProvider to fail back on,
 * when the system-users-library has not been provided in the classpath.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class EmptySystemUserProvider implements SystemUserProvider {

    @Inject
    private Logger logger;

    @Override
    public Optional<UUID> getContextSystemUserId() {
        logger.error("Could not fetch system user. system-users-library not available in the classpath");
        return empty();
    }
}
