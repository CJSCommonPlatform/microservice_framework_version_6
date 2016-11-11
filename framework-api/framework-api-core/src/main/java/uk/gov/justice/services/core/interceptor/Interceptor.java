package uk.gov.justice.services.core.interceptor;

/**
 * Interface that all interceptors must implement.
 */
public interface Interceptor {

    /**
     * Process an interception with the given {@link InterceptorContext} and {@link
     * InterceptorChain}.
     *
     * @param interceptorContext the interceptor context
     * @param interceptorChain   the interceptor chain, call this with processNext after completion
     *                           of task
     * @return an interceptor context
     */
    InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain);

    /**
     * Provides the priority level of an interceptor, lower is higher priority.  This is used to
     * set the calling sequence the chain of interceptors.
     *
     * @return the priority level of the interceptor
     */
    int priority();
}
