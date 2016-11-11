package uk.gov.justice.services.core.interceptor;

/**
 * Function set up in the {@link InterceptorChainProcessor} to call a dispatch Handles method.  Used
 * as the endpoint of the Interceptor Chain.
 */
@FunctionalInterface
public interface Target {

    InterceptorContext process(final InterceptorContext interceptorContext);
}
