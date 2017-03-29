package uk.gov.justice.services.core.audit;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Interceptor to capture audit events from local framework components.
 */
public class LocalAuditInterceptor implements Interceptor {

    @Inject
    AuditService auditService;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        recordAudit(interceptorContext.inputEnvelope());

        final InterceptorContext outputContext = interceptorChain.processNext(interceptorContext);
        final Optional<JsonEnvelope> jsonEnvelope = outputContext.outputEnvelope();

        jsonEnvelope.ifPresent(this::recordAudit);
        return outputContext;
    }

    private void recordAudit(final JsonEnvelope jsonEnvelope) {
        auditService.audit(jsonEnvelope);
    }
}