package uk.gov.justice.services.core.accesscontrol;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

public interface PolicyEvaluator {

    Optional<AccessControlViolation> checkAccessPolicyFor(final String component ,final JsonEnvelope jsonEnvelope);
}
