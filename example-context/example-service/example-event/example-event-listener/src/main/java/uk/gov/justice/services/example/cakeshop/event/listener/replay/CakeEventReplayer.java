package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.sourcing.subscription.EventReplayer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

public class CakeEventReplayer implements EventReplayer {

    @Override
    public void replay(final InterceptorChainProcessor interceptorChainProcessor) {

        final List<JsonEnvelope> jsonEnvelopes = new CakeFactory(10).generateEvents(1000);

        jsonEnvelopes.forEach(jsonEnvelope ->
                interceptorChainProcessor.process(InterceptorContext.interceptorContextWithInput(jsonEnvelope))
        );
    }

}
