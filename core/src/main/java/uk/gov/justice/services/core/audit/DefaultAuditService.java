package uk.gov.justice.services.core.audit;


import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Service responsible for hooking auditing into the framework.
 */
public class DefaultAuditService implements AuditService {

    @Inject
    @Value(key = "audit.blacklist", defaultValue = "")
    String auditBlacklist;

    @Inject
    AuditClient auditClient;

    @Inject
    Logger logger;

    private Pattern auditBlacklistPattern;

    @PostConstruct
    public void initialise() {
        auditBlacklistPattern = compile(auditBlacklist);
    }

    /**
     * Orchestrates the auditing of the action, uses a blacklist regex pattern to skip auditing if
     * required.
     *
     * @param envelope - the envelope to be audited.
     */
    public void audit(final JsonEnvelope envelope) {

        final String actionName = envelope.metadata().name();

        if (auditBlacklistPattern.matcher(actionName).matches()) {
            logger.info(format("Skipping auditing of action %s due to configured blacklist pattern %s.", actionName, auditBlacklist));
        } else {
            auditClient.auditEntry(envelope);
        }
    }
}
