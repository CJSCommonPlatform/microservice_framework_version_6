package uk.gov.justice.services.components.common;

import uk.gov.justice.services.core.accesscontrol.LocalAccessControlInterceptor;
import uk.gov.justice.services.core.audit.LocalAuditInterceptor;
import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProvider;
import uk.gov.justice.services.metrics.interceptor.IndividualActionMetricsInterceptor;
import uk.gov.justice.services.metrics.interceptor.TotalActionMetricsInterceptor;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public abstract class BaseInterceptorChainProvider implements InterceptorChainProvider {

    @Override
    public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
        final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = new LinkedList<>();
        interceptorChainTypes.add(new ImmutablePair<>(1, TotalActionMetricsInterceptor.class));
        interceptorChainTypes.add(new ImmutablePair<>(2, IndividualActionMetricsInterceptor.class));
        interceptorChainTypes.add(new ImmutablePair<>(3000, LocalAuditInterceptor.class));
        interceptorChainTypes.add(new ImmutablePair<>(4000, LocalAccessControlInterceptor.class));
        return interceptorChainTypes;
    }
}