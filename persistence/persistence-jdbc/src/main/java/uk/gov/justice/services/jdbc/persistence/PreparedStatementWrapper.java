package uk.gov.justice.services.jdbc.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Deque;
import java.util.LinkedList;

public class PreparedStatementWrapper implements AutoCloseable {
    private final PreparedStatement preparedStatement;
    private final Deque<AutoCloseable> closeables = new LinkedList<>();


    public static PreparedStatementWrapper valueOf(final Connection connection, final String queryTemplate) throws SQLException {
        PreparedStatementWrapper preparedStatementWrapper = null;
        try {
            preparedStatementWrapper = new PreparedStatementWrapper(connection, connection.prepareStatement(queryTemplate));
        } catch (SQLException sqlEx) {
            handle(sqlEx, connection);
        }
        return preparedStatementWrapper;
    }

    public void setObject(final int parameterIndex, final Object obj) throws SQLException {
        try {
            this.preparedStatement.setObject(parameterIndex, obj);
        } catch (SQLException e) {
            handle(e, this);
        }
    }

    public void setString(final int parameterIndex, final String str) throws SQLException {
        try {
            this.preparedStatement.setString(parameterIndex, str);
        } catch (SQLException e) {
            handle(e, this);
        }
    }

    public void setLong(final int parameterIndex, final Long lng) throws SQLException {
        try {
            this.preparedStatement.setLong(parameterIndex, lng);
        } catch (SQLException e) {
            handle(e, this);
        }
    }

    public void setInt(final int parameterIndex, final Integer value) throws SQLException {
        try {
            this.preparedStatement.setInt(parameterIndex, value);
        } catch (final SQLException e) {
            handle(e, this);
        }
    }

    public void setTimestamp(final int parameterIndex, final Timestamp timestamp) throws SQLException {
        try {
            this.preparedStatement.setTimestamp(parameterIndex, timestamp);
        } catch (SQLException e) {
            handle(e, this);
        }
    }

    public void setBoolean(final int parameterIndex, final boolean bool) throws SQLException {
        try {
            this.preparedStatement.setBoolean(parameterIndex, bool);
        } catch (SQLException e) {
            handle(e, this);
        }
    }

    public ResultSet executeQuery() throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = preparedStatement.executeQuery();
            this.closeables.addFirst(resultSet);
        } catch (SQLException e) {
            handle(e, this);
        }
        return resultSet;
    }

    public int executeUpdate() throws SQLException {
        int result = 0;
        try {
            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handle(e, this);
        }
        return result;
    }

    private PreparedStatementWrapper(final Connection connection, final PreparedStatement preparedStatement) {
        this.closeables.add(preparedStatement);
        this.closeables.add(connection);
        this.preparedStatement = preparedStatement;
    }

    private static void handle(final SQLException sqlEx, final AutoCloseable closeable) throws SQLException {
        try {
            closeable.close();
        } catch (Exception ex) {
            sqlEx.addSuppressed(ex);
        }
        throw sqlEx;
    }


    @Override
    public void close() {
        this.closeables.forEach(c -> {
            try (AutoCloseable c1 = c) {
            } catch (Exception e) {
                throw new JdbcRepositoryException(e);
            }
        });
    }
}
