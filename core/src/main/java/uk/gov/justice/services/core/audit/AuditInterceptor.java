package uk.gov.justice.services.core.audit;

import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Interceptor to capture audit events from local framework components.
 */
public class AuditInterceptor implements Interceptor {

    private static final int AUDIT_PRIORITY = 2000;

    @Inject
    AuditService auditService;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        if (isLocalComponentLocation(interceptorContext)) {
            recordAudit(interceptorContext.inputEnvelope());

            final InterceptorContext outputContext = interceptorChain.processNext(interceptorContext);
            final Optional<JsonEnvelope> jsonEnvelope = outputContext.outputEnvelope();

            jsonEnvelope.ifPresent(this::recordAudit);
            return outputContext;
        }

        return interceptorChain.processNext(interceptorContext);
    }

    @Override
    public int priority() {
        return AUDIT_PRIORITY;
    }

    private boolean isLocalComponentLocation(final InterceptorContext interceptorContext) {
        return LOCAL.equals(componentLocationFrom(interceptorContext.injectionPoint()));
    }

    private void recordAudit(final JsonEnvelope jsonEnvelope) {
        auditService.audit(jsonEnvelope);
    }
}
