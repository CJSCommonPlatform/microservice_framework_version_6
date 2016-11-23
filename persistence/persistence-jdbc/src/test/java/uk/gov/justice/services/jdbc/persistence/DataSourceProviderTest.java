package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DataSourceProviderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private InitialContextFactory initialContextFactory;

    @InjectMocks
    private DataSourceProvider dataSourceProvider;

    @Test
    public void shouldLookupTheDatasourceUsingTheInitialContext() throws Exception {

        final String jndiName = "the-jndi-name";

        final InitialContext initialContext = mock(InitialContext.class);
        final DataSource dataSource = mock(DataSource.class);

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup(jndiName)).thenReturn(dataSource);

        assertThat(dataSourceProvider.getDataSource(jndiName), is(dataSource));
    }

    @Test
    public void shouldThrowExceptionIfInitialContextThrowsNamingException() throws Exception {

        final String jndiName = "the-jndi-name";
        final InitialContext initialContext = mock(InitialContext.class);

        expectedException.expect(DataAccessException.class);
        expectedException.expectCause(any(NamingException.class));
        expectedException.expectMessage("Failed to get DataSource from container using JNDI name 'the-jndi-name'");

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup(jndiName)).thenThrow(new NamingException());

        dataSourceProvider.getDataSource(jndiName);
    }

    @Test
    public void shouldThrowExceptionIfInitialContextFactoryThrowsNamingException() throws Exception {

        final String jndiName = "the-jndi-name";

        expectedException.expect(DataAccessException.class);
        expectedException.expectCause(any(NamingException.class));
        expectedException.expectMessage("Failed to get InitialContext from container");

        when(initialContextFactory.create()).thenThrow(new NamingException());

        dataSourceProvider.getDataSource(jndiName);
    }
}
