package uk.gov.justice.services.example.provider;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.adapter.rest.interceptor.InputStreamFileInterceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntryProvider;

import java.util.ArrayList;
import java.util.List;

public class ExampleCommandApiInterceptorChainProvider implements InterceptorChainEntryProvider {

    final List<InterceptorChainEntry> interceptorChainEntries = new ArrayList<>();

    public ExampleCommandApiInterceptorChainProvider() {
        interceptorChainEntries.add(new InterceptorChainEntry(7000, InputStreamFileInterceptor.class));
    }

    @Override
    public String component() {
        return COMMAND_API;
    }

    @Override
    public List<InterceptorChainEntry> interceptorChainTypes() {
        return interceptorChainEntries;
    }
}
