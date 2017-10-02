package uk.gov.justice.services.components.common;

import uk.gov.justice.services.core.interceptor.InterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseInterceptorChainProvider implements InterceptorChainProvider {

    final List<InterceptorChainEntry> interceptorChainTypes = new LinkedList<>();

    public BaseInterceptorChainProvider() {
        interceptorChainTypes.add(new InterceptorChainEntry(1, TotalActionMetricsInterceptor.class));
        interceptorChainTypes.add(new InterceptorChainEntry(2, IndividualActionMetricsInterceptor.class));
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        return interceptorChainTypes;
    }
}
