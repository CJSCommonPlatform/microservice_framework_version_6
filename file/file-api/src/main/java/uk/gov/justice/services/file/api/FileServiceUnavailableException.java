package uk.gov.justice.services.file.api;

/**
 * Exception to be thrown when the file service is not available.
 */
public class FileServiceUnavailableException  extends RuntimeException {

    public FileServiceUnavailableException(final String message) {
        super(message);
    }

    public FileServiceUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
