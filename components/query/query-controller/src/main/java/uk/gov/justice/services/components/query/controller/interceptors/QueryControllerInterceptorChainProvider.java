package uk.gov.justice.services.components.query.controller.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class QueryControllerInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_CONTROLLER;
    }
}