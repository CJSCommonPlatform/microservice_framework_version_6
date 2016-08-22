package uk.gov.justice.services.core.accesscontrol;

import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.LOCAL;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Interceptor to apply access control to local framework components.
 */
public class AccessControlInterceptor implements Interceptor {

    private static final int ACCESS_CONTROL_PRIORITY = 6000;

    @Inject
    AccessControlService accessControlService;

    @Inject
    AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        if (isLocalComponentLocation(interceptorContext)) {
            checkAccessControl(interceptorContext.inputEnvelope());
        }

        return interceptorChain.processNext(interceptorContext);
    }

    @Override
    public int priority() {
        return ACCESS_CONTROL_PRIORITY;
    }

    private boolean isLocalComponentLocation(final InterceptorContext interceptorContext) {
        return LOCAL.equals(componentLocationFrom(interceptorContext.injectionPoint()));
    }

    private void checkAccessControl(final JsonEnvelope jsonEnvelope) {
        final Optional<AccessControlViolation> accessControlViolation = accessControlService.checkAccessControl(jsonEnvelope);

        if (accessControlViolation.isPresent()) {
            final String errorMessage = accessControlFailureMessageGenerator.errorMessageFrom(
                    jsonEnvelope,
                    accessControlViolation.get());

            throw new AccessControlViolationException(errorMessage);
        }
    }
}
