package uk.gov.justice.services.core.accesscontrol;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

public interface AccessControlService {

    Optional<AccessControlViolation> checkAccessControl(final String component, final JsonEnvelope jsonEnvelope);
}
