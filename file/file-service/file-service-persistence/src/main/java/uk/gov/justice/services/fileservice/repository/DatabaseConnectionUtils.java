package uk.gov.justice.services.fileservice.repository;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DatabaseConnectionUtils {

    public Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to get SQL Connection", e);
        }
    }

    public boolean getAutoCommit(final Connection connection) {

        try {
            return connection.getAutoCommit();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to get autocommit from jdbc Connection", e);
        }
    }

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

    public void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to commit transaction", e);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException("Failed to roll back transaction", e);
        }
    }
}
