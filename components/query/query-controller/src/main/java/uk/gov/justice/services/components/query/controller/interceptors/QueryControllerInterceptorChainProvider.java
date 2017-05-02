package uk.gov.justice.services.components.query.controller.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;
import uk.gov.justice.services.core.accesscontrol.LocalAccessControlInterceptor;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class QueryControllerInterceptorChainProvider extends BaseInterceptorChainProvider {

    public QueryControllerInterceptorChainProvider() {
        interceptorChainTypes().add(new ImmutablePair<>(4000, LocalAccessControlInterceptor.class));
    }

    @Override
    public String component() {
        return QUERY_CONTROLLER;
    }
}