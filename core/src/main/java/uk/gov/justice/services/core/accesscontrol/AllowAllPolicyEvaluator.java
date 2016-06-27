package uk.gov.justice.services.core.accesscontrol;

import static java.util.Optional.empty;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AllowAllPolicyEvaluator implements PolicyEvaluator {

    @Override
    public Optional<AccessControlViolation> checkAccessPolicyFor(@SuppressWarnings("unused") final JsonEnvelope jsonEnvelope) {
        return empty();
    }
}
