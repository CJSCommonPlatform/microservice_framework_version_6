package uk.gov.justice.services.test.utils.persistence;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import static java.util.Arrays.asList;

import static java.util.Collections.singletonList;
import static org.apache.openejb.testing.SingleApplicationComposerRunner.close;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.jdbc.persistence.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

public class DatabaseCleanerTest {

    private final TestJdbcConnectinProvider testJdbcConnectinProvider = mock(TestJdbcConnectinProvider.class);

    private DatabaseCleaner databaseCleaner = new DatabaseCleaner(testJdbcConnectinProvider);

    @Test
    public void shouldCleanSomeViewStoreTables() throws Exception {

        final String table_1 = "table_1";
        final String table_2 = "table_2";

        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(testJdbcConnectinProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + table_1)).thenReturn(preparedStatement_1);
        when(connection.prepareStatement("DELETE FROM " + table_2)).thenReturn(preparedStatement_2);

        databaseCleaner.cleanViewStoreTables(contextName, asList(table_1, table_2));

        verify(preparedStatement_1).executeUpdate();
        verify(preparedStatement_2).executeUpdate();

        verify(connection).close();
        verify(preparedStatement_1).close();
        verify(preparedStatement_2).close();
    }

    @Test
    public void shouldCleanTheEventLogTable() throws Exception {

        final String tableName = "event_log";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectinProvider.getEventStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);

        databaseCleaner.cleanEventLogTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCleanTheStreamBufferTable() throws Exception {

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectinProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);

        databaseCleaner.cleanStreamBufferTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldCleanTheStreamStatusTable() throws Exception {

        final String tableName = "stream_status";
        final String contextName = "my-context";

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectinProvider.getViewStoreConnection(contextName)).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM " + tableName)).thenReturn(preparedStatement);

        databaseCleaner.cleanStreamStatusTable(contextName);

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCleaningAViewStoreTableFails() throws Exception {

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);

        when(testJdbcConnectinProvider.getViewStoreConnection(contextName)).thenReturn(connection);
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

        when(testJdbcConnectinProvider.getEventStoreConnection(contextName)).thenReturn(connection);
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

        final String tableName = "stream_buffer";
        final String contextName = "my-context";

        final SQLException sqlException = new SQLException("Oops");

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(testJdbcConnectinProvider.getViewStoreConnection(contextName)).thenReturn(connection);
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

        when(testJdbcConnectinProvider.getEventStoreConnection(contextName)).thenReturn(connection);
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
