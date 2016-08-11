package uk.gov.justice.services.core.interceptor;

public interface Target {

    InterceptorContext process(final InterceptorContext interceptorContext);
}
