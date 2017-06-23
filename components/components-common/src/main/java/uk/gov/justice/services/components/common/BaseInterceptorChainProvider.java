package uk.gov.justice.services.components.common;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProvider;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public abstract class BaseInterceptorChainProvider implements InterceptorChainProvider {

    final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = new LinkedList<>();

    public BaseInterceptorChainProvider(){
        interceptorChainTypes.add(new ImmutablePair<>(1, TotalActionMetricsInterceptor.class));
        interceptorChainTypes.add(new ImmutablePair<>(2, IndividualActionMetricsInterceptor.class));
    }

    @Override
    public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
        return interceptorChainTypes;
    }
}
