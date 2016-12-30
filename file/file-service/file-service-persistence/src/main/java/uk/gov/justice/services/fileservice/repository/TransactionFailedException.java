package uk.gov.justice.services.fileservice.repository;

public class TransactionFailedException extends Exception {

    public TransactionFailedException(final String message) {
        super(message);
    }

    public TransactionFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
