package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.empty;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;

public class AccessControlService {

    static final String ACCESS_CONTROL_DISABLED_PROPERTY =
                    "uk.gov.justice.services.core.accesscontrol.disabled";

    @Inject
    PolicyEvaluator policyEvaluator;

    public Optional<AccessControlViolation> checkAccessControl(final JsonEnvelope jsonEnvelope) {

        if (accessControlDisabled()) {
            return empty();
        }

        return policyEvaluator.checkAccessPolicyFor(jsonEnvelope);
    }

    private boolean accessControlDisabled() {
        return "true".equals(System.getProperty(ACCESS_CONTROL_DISABLED_PROPERTY));
    }
}
