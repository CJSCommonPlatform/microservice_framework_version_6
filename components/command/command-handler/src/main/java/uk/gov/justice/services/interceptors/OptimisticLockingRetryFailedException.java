package uk.gov.justice.services.interceptors;

/**
 * Exception thrown when the retry interceptor fails to complete after reaching the maximum number
 * of retries.
 */
public class OptimisticLockingRetryFailedException extends RuntimeException {

    private static final long serialVersionUID = -3290663041510311855L;

    public OptimisticLockingRetryFailedException(final String message) {
        super(message);
    }
}