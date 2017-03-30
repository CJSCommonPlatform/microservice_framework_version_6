package uk.gov.justice.services.core.interceptor;

@FunctionalInterface
public interface Target {

    InterceptorContext process(final InterceptorContext interceptorContext);
}
