package uk.gov.justice.services.core.interceptor;

import static java.util.stream.Collectors.toList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class DefaultInterceptorChain implements InterceptorChain {

    private Deque<Interceptor> interceptors;
    private Target target;

    public DefaultInterceptorChain(final Deque<Interceptor> interceptors, final Target target) {
        this.interceptors = interceptors;
        this.target = target;
    }

    public InterceptorContext processNext(final InterceptorContext interceptorContext) {
        if (interceptors.isEmpty()) {
            return target.process(interceptorContext);
        }

        return interceptors.poll().process(interceptorContext, copyOfInterceptorChain());
    }

    public List<InterceptorContext> processNext(final Stream<InterceptorContext> interceptorContexts) {
        try (final Stream<InterceptorContext> interceptorContextStream = interceptorContexts
                .map(interceptorContext -> copyOfInterceptorChain().processNext(interceptorContext))) {
            return interceptorContextStream.collect(toList());
        }
    }

    @SuppressWarnings("unchecked")
    private InterceptorChain copyOfInterceptorChain() {
        return new DefaultInterceptorChain(new LinkedList<>(interceptors), target);
    }
}
