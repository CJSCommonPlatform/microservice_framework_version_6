package uk.gov.justice.services.core.audit;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Interface for audit clients.
 */
public interface AuditClient {

    /**
     * Record an audit entry.
     * @param envelope the message to audit
     * @param component the component that generated the audit entry
     */
    void auditEntry(final JsonEnvelope envelope, final String component);
}
