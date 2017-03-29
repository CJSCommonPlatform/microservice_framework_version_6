package uk.gov.justice.services.example.provider;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class ExampleCustomApiInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return "CUSTOM_API";
    }
}