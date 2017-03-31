package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;

import uk.gov.justice.services.core.interceptor.BaseInterceptorChainProvider;

public class EventApiInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_API;
    }
}