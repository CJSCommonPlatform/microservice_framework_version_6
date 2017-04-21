package uk.gov.justice.services.fileservice.repository;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

/**
 * Utility class for closing java {@link AutoCloseable}s, as try with resources is so
 * damn clunky when used for closing Connections, PreparedStatements and ResultSets.
 *
 * Will handle a possibly null AutoClosable and the possible {@link java.sql.SQLException} on the
 * {@code close()} method.
 */
public class Closer {

    /**
     * Closes at least one {@link AutoCloseable}
     * @param autoCloseable at least one {@link AutoCloseable} to be closed
     * @param autoCloseables any other {@link AutoCloseable} to be closeds
     */
    public void close(final AutoCloseable autoCloseable, final AutoCloseable... autoCloseables) {
        close(autoCloseable);
        for(final AutoCloseable closeable: autoCloseables) {
            close(closeable);
        }
    }

    private void close(final AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }

        try {
           autoCloseable.close();
        } catch (final Exception e) {
            throw new JdbcRepositoryException("Failed to close JDBC Connection", e);
        }
    }
}
