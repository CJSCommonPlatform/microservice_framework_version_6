package uk.gov.justice.services.fileservice.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

public class DatabaseConnectionUtilsTest {

    private DatabaseConnectionUtils databaseConnectionUtils = new DatabaseConnectionUtils();

    @Test
    public void shouldGetAConnectionFromADatasource() throws Exception {

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(databaseConnectionUtils.getConnection(dataSource), is(connection));
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfGettingAConnectionThrowsAnSqlException() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        final SQLException sqlException = new SQLException("Oooops");

        when(dataSource.getConnection()).thenThrow(sqlException);

        try {
            databaseConnectionUtils.getConnection(dataSource);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to get SQL Connection from datasource"));
            assertThat(expected.getCause(), is(sqlException));
        }
    }

    @Test
    public void shouldGetAutoCommitStatusFromAConnection() throws Exception {

        final boolean autoCommit = true;
        final Connection connection = mock(Connection.class);

        when(connection.getAutoCommit()).thenReturn(autoCommit);

        assertThat(databaseConnectionUtils.getAutoCommit(connection), is(autoCommit));
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfGettingAutoCommitStatusFromAConnectionThrowsAnSqlException() throws Exception {

        final SQLException sqlException = new SQLException("Oooops");
        final Connection connection = mock(Connection.class);

        when(connection.getAutoCommit()).thenThrow(sqlException);

        try {
            databaseConnectionUtils.getAutoCommit(connection);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to get autocommit from jdbc Connection"));
            assertThat(expected.getCause(), is(sqlException));
        }
    }

    @Test
    public void shouldSetAutoCommitOnAConnection() throws Exception {

        final boolean autoCommit = true;
        final Connection connection = mock(Connection.class);

        databaseConnectionUtils.setAutoCommit(autoCommit, connection);

        verify(connection).setAutoCommit(autoCommit);
    }

    @Test
    public void shouldHandleANullConnectionWhenSettingAutoCommit() throws Exception {

        final boolean autoCommit = true;
        final Connection connection = null;

        databaseConnectionUtils.setAutoCommit(autoCommit, connection);
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfSettingAutoCommitStatusFromAConnectionThrowsAnSqlException() throws Exception {

        final boolean autoCommit = true;
        final SQLException sqlException = new SQLException("Oooops");
        final Connection connection = mock(Connection.class);

        doThrow(sqlException).when(connection).setAutoCommit(autoCommit);

        try {
            databaseConnectionUtils.setAutoCommit(autoCommit, connection);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to set autocommit on jdbc Connection"));
            assertThat(expected.getCause(), is(sqlException));
        }
    }

    @Test
    public void shouldCommitATransactionOnAConnection() throws Exception {

        final Connection connection = mock(Connection.class);

        databaseConnectionUtils.commit(connection);

        verify(connection).commit();
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfCommittingAConnectionThrowsAnSqlException() throws Exception {

        final SQLException sqlException = new SQLException("Oooops");
        final Connection connection = mock(Connection.class);

        doThrow(sqlException).when(connection).commit();

        try {
            databaseConnectionUtils.commit(connection);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to commit transaction"));
            assertThat(expected.getCause(), is(sqlException));
        }
    }

    @Test
    public void shouldRollbackATransactionOnAConnection() throws Exception {

        final Connection connection = mock(Connection.class);

        databaseConnectionUtils.rollback(connection);

        verify(connection).rollback();
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfRollingBackAConnectionThrowsAnSqlException() throws Exception {

        final SQLException sqlException = new SQLException("Oooops");
        final Connection connection = mock(Connection.class);

        doThrow(sqlException).when(connection).rollback();

        try {
            databaseConnectionUtils.rollback(connection);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getMessage(), is("Failed to roll back transaction"));
            assertThat(expected.getCause(), is(sqlException));
        }
    }
}
