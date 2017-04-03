package uk.gov.justice.services.core.interceptor;

import java.util.List;
import java.util.stream.Stream;

public interface InterceptorChain {

    /**
     * Process the next {@link Interceptor} on the queue.
     *
     * @param interceptorContext the {@link InterceptorContext} to pass onto the next interceptor in
     *                           the chain
     * @return the interceptor context returned from the interceptor chain
     */
    InterceptorContext processNext(final InterceptorContext interceptorContext);

    /**
     * Process each interceptor context in a stream with all the remaining interceptors on the
     * queue.
     *
     * @param interceptorContexts the stream of {@link InterceptorContext} to process
     * @return a list of processed interceptor contexts
     */
    List<InterceptorContext> processNext(final Stream<InterceptorContext> interceptorContexts);
}
