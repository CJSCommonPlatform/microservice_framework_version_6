package uk.gov.justice.services.components.command.controller.interceptors;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class CommandControllerInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_CONTROLLER;
    }
}