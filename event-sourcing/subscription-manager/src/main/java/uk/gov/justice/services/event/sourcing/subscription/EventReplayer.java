package uk.gov.justice.services.event.sourcing.subscription;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;

public interface EventReplayer {

    void replay(final InterceptorChainProcessor interceptorChainProcessor);
}
