package uk.gov.justice.services.core.interceptor.exception;

public class InterceptorCacheException extends RuntimeException {
    private static final long serialVersionUID = 987796123212684089L;

    public InterceptorCacheException(final String message) {
        super(message);
    }
}