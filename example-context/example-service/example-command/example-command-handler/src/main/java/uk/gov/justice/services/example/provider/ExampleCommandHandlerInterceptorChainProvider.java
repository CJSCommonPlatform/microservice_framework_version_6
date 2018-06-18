package uk.gov.justice.services.example.provider;

import static java.lang.Integer.MAX_VALUE;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.components.command.handler.interceptors.RetryInterceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;

import java.util.ArrayList;
import java.util.List;

public class ExampleCommandHandlerInterceptorChainProvider implements InterceptorChainEntryProvider {

    private final List<InterceptorChainEntry> interceptorChainEntries = new ArrayList<>();

    public ExampleCommandHandlerInterceptorChainProvider() {
        interceptorChainEntries.add(new InterceptorChainEntry(MAX_VALUE, RetryInterceptor.class));
    }

    @Override
    public String component() {
        return COMMAND_HANDLER;
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        return interceptorChainEntries;
    }
}
