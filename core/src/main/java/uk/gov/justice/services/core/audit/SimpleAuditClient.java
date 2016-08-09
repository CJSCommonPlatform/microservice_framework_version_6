package uk.gov.justice.services.core.audit;


import uk.gov.justice.services.core.configuration.AppNameProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;

@ApplicationScoped
@Alternative
@Priority(1)
public class SimpleAuditClient {

    @Inject
    Logger logger;

    @Inject
    AppNameProvider appNameProvider;

    public void auditEntry(final JsonEnvelope envelope) {
        logger.info(createAuditMessageFrom(envelope));
    }

    private String createAuditMessageFrom(final JsonEnvelope envelope) {

        return new JSONObject()
                .put("appName", appNameProvider.getAppName())
                .put("envelope", new JSONObject(envelope.toString()))
                .toString(2);
    }
}
