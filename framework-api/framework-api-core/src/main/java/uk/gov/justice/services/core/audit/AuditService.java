package uk.gov.justice.services.core.audit;

import uk.gov.justice.services.messaging.JsonEnvelope;

/**
 * Service responsible for hooking auditing into the framework.
 */
public interface AuditService {

    /**
     * Orchestrates the auditing of the action.
     *
     * @param envelope - the envelope to be audited.
     */
    void audit(final JsonEnvelope envelope);
}
