package uk.gov.justice.services.core.accesscontrol;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class AccessControlFailureMessageGenerator {

    public String errorMessageFrom(
                    final JsonEnvelope jsonEnvelope,
                    final AccessControlViolation accessControlViolation) {

        return format("Access Control failed for json envelope '%s'. Reason: %s",
                        jsonEnvelope,
                        accessControlViolation.getReason());
    }
}
