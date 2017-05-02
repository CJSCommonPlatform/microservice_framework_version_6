package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.empty;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;

public class DefaultAccessControlService implements AccessControlService {

    static final String ACCESS_CONTROL_DISABLED_PROPERTY =
            "uk.gov.justice.services.core.accesscontrol.disabled";

    @Inject
    Logger logger;

    @Inject
    PolicyEvaluator policyEvaluator;

    public Optional<AccessControlViolation> checkAccessControl(final String component, final JsonEnvelope jsonEnvelope) {

        if (accessControlDisabled()) {
            logger.trace("Skipping access control due to configuration");
            return empty();
        }

        logger.trace("Performing access control for action: {}", jsonEnvelope.metadata().name());
        return policyEvaluator.checkAccessPolicyFor(component,jsonEnvelope);
    }

    private boolean accessControlDisabled() {
        return "true".equals(System.getProperty(ACCESS_CONTROL_DISABLED_PROPERTY));
    }
}
