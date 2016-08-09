package uk.gov.justice.services.core.audit;


import uk.gov.justice.services.core.configuration.ServiceContextNameProvider;
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
public class SimpleAuditClient implements AuditClient {

    @Inject
    Logger logger;

    @Inject
    ServiceContextNameProvider serviceContextNameProvider;

    @Override
    public void auditEntry(final JsonEnvelope envelope) {
        logger.info(createAuditMessageFrom(envelope));
    }

    private String createAuditMessageFrom(final JsonEnvelope envelope) {

        return new JSONObject()
                .put("serviceContext", serviceContextNameProvider.getServiceContextName())
                .put("envelope", new JSONObject(envelope.toString()))
                .toString(2);
    }
}
