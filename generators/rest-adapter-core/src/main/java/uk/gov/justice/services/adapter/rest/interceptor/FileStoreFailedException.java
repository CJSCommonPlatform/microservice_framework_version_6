package uk.gov.justice.services.adapter.rest.interceptor;

public class FileStoreFailedException extends RuntimeException {

    public FileStoreFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
