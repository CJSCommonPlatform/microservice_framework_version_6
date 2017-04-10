package uk.gov.justice.services.components.query.api.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class QueryApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_API;
    }
}