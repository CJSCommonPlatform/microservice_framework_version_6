package uk.gov.justice.services.core.interceptor;

import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class InterceptorChain {

    private Deque<Interceptor> interceptors;
    private Target target;

    public InterceptorChain(final Deque<Interceptor> interceptors, final Target target) {
        this.interceptors = interceptors;
        this.target = target;
    }

    /**
     * Process the next {@link Interceptor} on the queue.
     *
     * @param interceptorContext the {@link InterceptorContext} to pass onto the next interceptor in
     *                           the chain
     * @return the interceptor context returned from the interceptor chain
     */
    public InterceptorContext processNext(final InterceptorContext interceptorContext) {
        if (interceptors.isEmpty()) {
            return target.process(interceptorContext);
        }

        return interceptors.poll().process(interceptorContext, copyOfInterceptorChain());
    }

    /**
     * Process each interceptor context in a stream with all the remaining interceptors on the queue.
     *
     * @param interceptorContexts the stream of {@link InterceptorContext} to process
     * @return a list of processed interceptor contexts
     */
    public List<InterceptorContext> processNext(final Stream<InterceptorContext> interceptorContexts) {
        return interceptorContexts
                .map(interceptorContext -> copyOfInterceptorChain().processNext(interceptorContext))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private InterceptorChain copyOfInterceptorChain() {
        return new InterceptorChain(new LinkedList<>(interceptors), target);
    }
}
