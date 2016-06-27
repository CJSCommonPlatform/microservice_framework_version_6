package uk.gov.justice.services.core.accesscontrol;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper;

import javax.inject.Inject;

public class AccessControlFailureMessageGenerator {

    @Inject
    JsonEnvelopeLoggerHelper jsonEnvelopeLoggerHelper;

    public String errorMessageFrom(
                    final JsonEnvelope jsonEnvelope,
                    final AccessControlViolation accessControlViolation) {

        return format("Access Control failed for json envelope '%s'. Reason: %s",
                        jsonEnvelopeLoggerHelper.toTraceString(jsonEnvelope),
                        accessControlViolation.getReason());
    }
}
