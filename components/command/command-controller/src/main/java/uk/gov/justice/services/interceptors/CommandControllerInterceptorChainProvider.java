package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

import uk.gov.justice.services.core.interceptor.BaseInterceptorChainProvider;

public class CommandControllerInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_CONTROLLER;
    }
}