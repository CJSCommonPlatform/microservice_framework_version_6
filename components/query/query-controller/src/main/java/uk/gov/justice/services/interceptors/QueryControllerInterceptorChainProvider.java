package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class QueryControllerInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_CONTROLLER;
    }
}