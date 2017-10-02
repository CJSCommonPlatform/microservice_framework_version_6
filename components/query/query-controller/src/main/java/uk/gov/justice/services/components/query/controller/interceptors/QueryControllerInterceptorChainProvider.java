package uk.gov.justice.services.components.query.controller.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;
import uk.gov.justice.services.core.accesscontrol.LocalAccessControlInterceptor;
import uk.gov.justice.services.core.audit.LocalAuditInterceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChainEntry;

public class QueryControllerInterceptorChainProvider extends BaseInterceptorChainProvider {

    public QueryControllerInterceptorChainProvider() {
        interceptorChainTypes().add(new InterceptorChainEntry(3000, LocalAuditInterceptor.class));
        interceptorChainTypes().add(new InterceptorChainEntry(4000, LocalAccessControlInterceptor.class));
    }

    @Override
    public String component() {
        return QUERY_CONTROLLER;
    }
}
