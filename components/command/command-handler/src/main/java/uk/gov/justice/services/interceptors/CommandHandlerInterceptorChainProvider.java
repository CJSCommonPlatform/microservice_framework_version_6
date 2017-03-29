package uk.gov.justice.services.interceptors;

import static java.lang.Integer.MAX_VALUE;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.Interceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CommandHandlerInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_HANDLER;
    }

    @Override
    public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
        final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = super.interceptorChainTypes();
        interceptorChainTypes.add(new ImmutablePair<>(MAX_VALUE, RetryInterceptor.class));
        return interceptorChainTypes;
    }
}