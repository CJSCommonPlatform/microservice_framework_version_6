package uk.gov.justice.services.core.interceptor.spi;

import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWith;
import static uk.gov.justice.services.core.interceptor.DefaultContextPayload.contextPayloadWithNoEnvelope;

import uk.gov.justice.services.core.interceptor.DefaultInterceptorContext;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;


public class DefaultInterceptorContextProvider implements InterceptorContextProvider {

    @Override
    public InterceptorContext interceptorContextWithInput(final JsonEnvelope inputEnvelope) {
        return new DefaultInterceptorContext(contextPayloadWith(inputEnvelope), contextPayloadWithNoEnvelope());
    }

}
