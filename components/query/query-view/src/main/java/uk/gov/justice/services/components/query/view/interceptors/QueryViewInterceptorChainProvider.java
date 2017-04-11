package uk.gov.justice.services.components.query.view.interceptors;

import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class QueryViewInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return QUERY_VIEW;
    }
}