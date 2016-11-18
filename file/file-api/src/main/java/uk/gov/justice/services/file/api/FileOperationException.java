package uk.gov.justice.services.file.api;

/**
 * Exception to be thrown when the file service is not available.
 */
public class FileOperationException extends RuntimeException {

    public FileOperationException(final String message) {
        super(message);
    }

    public FileOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
