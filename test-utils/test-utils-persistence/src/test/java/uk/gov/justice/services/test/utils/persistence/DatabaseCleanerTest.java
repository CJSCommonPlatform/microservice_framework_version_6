package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Test;

public class DatabaseCleanerTest {

    private final TestJdbcConnectionProvider testJdbcConnectionProvider = mock(TestJdbcConnectionProvider.class);

    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner(testJdbcConnectionProvider);

    @Test
    public void shouldCleanSomeViewStoreTables() throws Exception {

        final String table_1 = "table_1";
        final String table_2 = "table_2";

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + table_1)).thenReturn(preparedStatement_1);
        when(connection.prepareStatement("DELETE FROM " + table_2)).thenReturn(preparedStatement_2);

        databaseCleaner.cleanViewStoreTables(contextName, table_1, table_2);

        verify(preparedStatement_1).executeUpdate();
        verify(preparedStatement_2).executeUpdate();

        verify(connection).close();
        verify(preparedStatement_1).close();
        verify(preparedStatement_2).close();
    }

    @Test
    public void shouldCleanSomeViewStoreTablesUsingVarArgMethod() throws Exception {

        final String table_1 = "table_1";
        final String table_2 = "table_2";

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + table_1)).thenReturn(preparedStatement_1);
        when(connection.prepareStatement("DELETE FROM " + table_2)).thenReturn(preparedStatement_2);

        databaseCleaner.cleanViewStoreTables(contextName, table_1, table_2);

        verify(preparedStatement_1).executeUpdate();
        verify(preparedStatement_2).executeUpdate();

        verify(connection).close();
        verify(preparedStatement_1).close();
        verify(preparedStatement_2).close();
    }

    @Test
    public void shouldCleanTheEventTable() throws Exception {

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + "event_log")).thenReturn(preparedStatement);
        when(connection.prepareStatement("DELETE FROM " + "event_stream")).thenReturn(preparedStatement);
        when(connection.prepareStatement("DELETE FROM " + "publish_queue")).thenReturn(preparedStatement);

        databaseCleaner.cleanEventStoreTables(contextName);

        verify(preparedStatement, times(3)).executeUpdate();
        verify(connection).close();
        verify(preparedStatement, times(3)).close();
    }

    @Test
    public void shouldCleanTheStreamBufferTable() throws Exception {

        final String tableName = "event_buffer";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);

        databaseCleaner.cleanStreamBufferTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCleanTheSubscriptionTable() throws Exception {

        final String tableName = "subscription";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);

        databaseCleaner.cleanSubscriptionTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCleaningAViewStoreTableFails() throws Exception {

        final String tableName = "event_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenThrow(sqlException);

        try {
            databaseCleaner.cleanStreamBufferTable(contextName);
            fail();
        } catch (DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to delete content from table " + tableName));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCleaningTheEventStoreTableFails() throws Exception {

        final String tableName = "event_log";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenThrow(sqlException);

        try {
            databaseCleaner.cleanEventLogTable(contextName);
            fail();
        } catch (DataAccessException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to delete content from table " + tableName));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADatAccessExceptionIfClosingTheViewStoreConnectionFails() throws Exception {

        final String tableName = "event_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);
        doThrow(sqlException).when(preparedStatement).close();

        try {
            databaseCleaner.cleanStreamBufferTable(contextName);
            fail();
        } catch (Exception expected) {
            assertThat(expected.getCause(), is(sqlException));
        }

        verify(connection).close();
    }

    @Test
    public void shouldThrowADatAccessExceptionIfClosingTheEventStoreConnectionFails() throws Exception {

        final String tableName = "event_log";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectionProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);
        doThrow(sqlException).when(preparedStatement).close();

        try {
            databaseCleaner.cleanEventLogTable(contextName);
            fail();
        } catch (Exception expected) {
            assertThat(expected.getCause(), is(sqlException));
        }

        verify(connection).close();
    }
}
