package uk.gov.justice.services.fileservice.repository;

/**
 * Exception which gets thrown on any failed database operation meaning that the current transaction
 * should be rolled back.
 *
 * This should remain as a checked exception forcing the caller of this class to catch the
 * exception and so roll back the transaction.
 */
public class TransactionFailedException extends Exception {

    public TransactionFailedException(final String message) {
        super(message);
    }

    public TransactionFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
