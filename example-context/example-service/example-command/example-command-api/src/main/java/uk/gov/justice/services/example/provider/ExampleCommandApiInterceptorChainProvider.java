package uk.gov.justice.services.example.provider;

import uk.gov.justice.services.adapter.rest.interceptor.InputStreamFileInterceptor;
import uk.gov.justice.services.components.command.api.interceptors.CommandApiInterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.Interceptor;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ExampleCommandApiInterceptorChainProvider extends CommandApiInterceptorChainProvider {

    @Override
    public List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes() {
        final List<Pair<Integer, Class<? extends Interceptor>>> interceptorChainTypes = super.interceptorChainTypes();
        interceptorChainTypes.add(new ImmutablePair<>(7000, InputStreamFileInterceptor.class));
        return interceptorChainTypes;
    }
}