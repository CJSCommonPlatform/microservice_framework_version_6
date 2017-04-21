package uk.gov.justice.services.fileservice.repository;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * A collection of handy methods that wrap database connection methods to allow the calling
 * class to not have to worry about exception handling or possible nullness
 */
public class DatabaseConnectionUtils {

    /**
     * Gets a database connection from a DataSource. Handles any possible SQLExceptions by wrapping
     * them in a {@link JdbcRepositoryException}
     *
     * @param dataSource the data source to get the Connection from
     * @return the JDBC Connection
     */
    public Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to get SQL Connection from datasource", e);
        }
    }

    /**
     * Gets the auto commit status of a Connection. Handles any possible SQLExceptions by wrapping
     * them in a {@link JdbcRepositoryException}
     *
     * @param connection the JDBC Connection from which to get the auto commit status
     * @return the auto commit status of that Connection
     */
    public boolean getAutoCommit(final Connection connection) {

        try {
            return connection.getAutoCommit();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to get autocommit from jdbc Connection", e);
        }
    }

    /**
     * Sets the auto commit status of a Connection. Handles any possible SQLExceptions by wrapping
     * them in a {@link JdbcRepositoryException}
     *
     * @param autoCommit the new auto commit status to set
     * @param connection the JDBC Connection on which to set the auto commit status
     */
    public void setAutoCommit(final boolean autoCommit, final Connection connection) {

        if(connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to set autocommit on jdbc Connection", e);
        }
    }

    /**
     * Commits the transaction of a JDBC Connection. Handles any possible SQLExceptions by wrapping
     * them in a {@link JdbcRepositoryException}
     *
     * @param connection the connection who's transaction should be committed.
     */
    public void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to commit transaction", e);
        }
    }

    /**
     * Rolls back a transaction of a JDBC Connection. Handles any possible SQLExceptions by wrapping
     * them in a {@link JdbcRepositoryException}
     *
     * @param connection the connection who's transaction should be rolled back.
     */
    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to roll back transaction", e);
        }
    }
}
