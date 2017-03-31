package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.interceptor.BaseInterceptorChainProvider;

public class CommandApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_API;
    }
}