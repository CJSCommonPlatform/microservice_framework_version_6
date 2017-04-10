package uk.gov.justice.services.example.provider;


import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class ExampleCustomApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return "CUSTOM_API";
    }
}