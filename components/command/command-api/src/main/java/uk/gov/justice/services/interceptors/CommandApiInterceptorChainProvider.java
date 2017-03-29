package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class CommandApiInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_API;
    }
}