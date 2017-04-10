package uk.gov.justice.services.components.command.api.interceptors;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class CommandApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return COMMAND_API;
    }
}