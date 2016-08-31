package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JdbcConnectionProviderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DataSourceProvider dataSourceProvider;

    @InjectMocks
    private JdbcConnectionProvider jdbcConnectionProvider;

    @Test
    public void shouldCreateAConnectionToTheContextsEventStore() throws Exception {

        final String contextName = "a-context";
        final String expectedJndiName = "java:/app/a-context-command-handler/DS.eventstore";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource(expectedJndiName)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(jdbcConnectionProvider.getEventStoreConnection(contextName), is(connection));
    }

    @Test
    public void shouldThrowADataAccessExceptionIfCreatingTheConnectionToTheEventStoreFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");

        final String contextName = "a-context";
        final String expectedJndiName = "java:/app/a-context-command-handler/DS.eventstore";

        final DataSource dataSource = mock(DataSource.class);

        when(dataSourceProvider.getDataSource(expectedJndiName)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(sqlException);

        expectedException.expect(DataAccessException.class);
        expectedException.expectCause(is(sqlException));
        expectedException.expectMessage("Failed to get sql Connection using JNDI name " + expectedJndiName);

        jdbcConnectionProvider.getEventStoreConnection(contextName);
    }

    @Test
    public void shouldCreateAConnectionToTheContextsViewStore() throws Exception {

        final String contextName = "a-context";
        final String expectedJndiName = "java:/DS.a-context";

        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        when(dataSourceProvider.getDataSource(expectedJndiName)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(jdbcConnectionProvider.getViewStoreConnection(contextName), is(connection));
    }


    @Test
    public void shouldThrowADataAccessExceptionIfCreatingTheConnectionToTheViewStoreFails() throws Exception {

        final SQLException sqlException = new SQLException("Oops");

        final String contextName = "a-context";
        final String expectedJndiName = "java:/DS.a-context";

        final DataSource dataSource = mock(DataSource.class);

        when(dataSourceProvider.getDataSource(expectedJndiName)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(sqlException);

        expectedException.expect(DataAccessException.class);
        expectedException.expectCause(is(sqlException));
        expectedException.expectMessage("Failed to get sql Connection using JNDI name " + expectedJndiName);

        jdbcConnectionProvider.getViewStoreConnection(contextName);
    }

}
