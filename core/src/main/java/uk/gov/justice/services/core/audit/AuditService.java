package uk.gov.justice.services.core.audit;


import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

public class AuditService {

    @Inject
    AuditClient auditClient;

    public void audit(final JsonEnvelope envelope) {
        auditClient.auditEntry(envelope);
    }
}
