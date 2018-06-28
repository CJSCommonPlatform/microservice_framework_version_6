package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

@Stateless
public class AsyncCakeEventReplayer {

    @Asynchronous
    public void replay(final JsonEnvelope jsonEnvelope,
                       final InterceptorChainProcessor interceptorChainProcessor) {
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
    }
}
