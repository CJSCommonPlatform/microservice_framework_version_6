package uk.gov.justice.services.example.cakeshop.event.listener.replay;

import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

@Stateless
public class AsyncCakeEventReplayer {

    @Asynchronous
    public Future<Optional<JsonEnvelope>> replay(final JsonEnvelope jsonEnvelope,
                         final InterceptorChainProcessor interceptorChainProcessor) {
        final Optional<JsonEnvelope> process = interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));

        return new AsyncResult(process);
    }
}
