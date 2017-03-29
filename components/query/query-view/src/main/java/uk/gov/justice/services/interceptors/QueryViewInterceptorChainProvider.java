package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class QueryViewInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_VIEW;
    }
}