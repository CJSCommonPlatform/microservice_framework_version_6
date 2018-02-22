package uk.gov.justice.services.example.cakeshop.event.listener.interceptor;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;

import javax.json.JsonObject;

/**
 * Interceptor triggers exception to test app behaviour after exception (connection leak etc)
 */
public class ExceptionThrowingInterceptor implements Interceptor {

    @Override
    public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
        final JsonObject payload = interceptorContext.inputEnvelope().payloadAsJsonObject();
        if (payload.containsKey("name") && payload.getString("name").contains("Exceptional cake")) {
            throw new TestInterceptorException();
        }
        return interceptorChain.processNext(interceptorContext);
    }
}
