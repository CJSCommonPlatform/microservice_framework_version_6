package uk.gov.justice.services.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.interceptor.DefaultInterceptorChainProvider;

public class EventProcessorInterceptorChainProvider extends DefaultInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_PROCESSOR;
    }
}
