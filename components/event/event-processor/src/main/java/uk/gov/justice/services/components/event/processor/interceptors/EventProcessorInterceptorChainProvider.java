package uk.gov.justice.services.components.event.processor.interceptors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.components.common.BaseInterceptorChainProvider;

public class EventProcessorInterceptorChainProvider extends BaseInterceptorChainProvider {

    @Override
    public String component() {
        return EVENT_PROCESSOR;
    }
}
