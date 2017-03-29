package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class EventApiInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_API;
    }
}