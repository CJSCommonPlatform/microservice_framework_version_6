package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class QueryApiInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_API;
    }
}