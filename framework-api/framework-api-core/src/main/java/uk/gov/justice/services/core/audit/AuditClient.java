package uk.gov.justice.services.core.audit;

import uk.gov.justice.services.messaging.JsonEnvelope;

public interface AuditClient {

    void auditEntry(final JsonEnvelope envelope);
}
