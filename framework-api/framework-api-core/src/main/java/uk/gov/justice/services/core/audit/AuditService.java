package uk.gov.justice.services.core.audit;

import uk.gov.justice.services.messaging.JsonEnvelope;

public interface AuditService {

    /**
     * Orchestrates the auditing of the action.
     *
     * @param envelope - the envelope to be audited.
     */
    void audit(final JsonEnvelope envelope);
}
