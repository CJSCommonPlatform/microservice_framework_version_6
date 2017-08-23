package uk.gov.justice.services.components.command.handler.interceptors;

import static java.lang.Integer.MAX_VALUE;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;
import uk.gov.justice.services.core.audit.LocalAuditInterceptor;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class CommandHandlerInterceptorChainProvider extends BaseInterceptorChainProvider {

    public CommandHandlerInterceptorChainProvider() {
        interceptorChainTypes().add(new ImmutablePair<>(MAX_VALUE, RetryInterceptor.class));
        interceptorChainTypes().add(new ImmutablePair<>(3000, LocalAuditInterceptor.class));
    }

    @Override
    public String component() {
        return COMMAND_HANDLER;
    }
}
