package uk.gov.justice.services.components.query.api.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class EventApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_API;
    }
}