package uk.gov.justice.subscription.jms.interceptor;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

public class StubEventFilterInterceptor implements Interceptor {

    @Override
    public InterceptorContext process(
            final InterceptorContext interceptorContext,
            final InterceptorChain interceptorChain) {
        return null;
    }
}
