package uk.gov.justice.services.example.provider;

import uk.gov.justice.services.adapter.rest.interceptor.InputStreamFileInterceptor;
import uk.gov.justice.services.components.command.api.interceptors.CommandApiInterceptorChainProvider;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;

import java.util.List;

public class ExampleCommandApiInterceptorChainProvider extends CommandApiInterceptorChainProvider {

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        final List<InterceptorChainEntry> interceptorChainTypes = super.interceptorChainTypes();
        interceptorChainTypes.add(new InterceptorChainEntry(7000, InputStreamFileInterceptor.class));
        return interceptorChainTypes;
    }
}
