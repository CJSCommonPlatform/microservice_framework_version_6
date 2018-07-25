package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcDataSourceProviderTest {

    @Mock
    private InitialContext initialContext;

    @InjectMocks
    private DefaultJdbcDataSourceProvider defaultJdbcDataSourceProvider;

    @Test
    public void shouldLookupTheDataSourceInTheInitialContextAndCache() throws Exception {

        final String jndiName = "the-jndi-name";

        final DataSource dataSource = mock(DataSource.class);

        when(initialContext.lookup(jndiName)).thenReturn(dataSource);

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));

        verify(initialContext, times(1)).lookup(jndiName);
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfTheLookupOfTheDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException("Ooops");

        final String jndiName = "the-jndi-name";

        when(initialContext.lookup(jndiName)).thenThrow(namingException);

        try {
            defaultJdbcDataSourceProvider.getDataSource(jndiName);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup DataSource using jndi name 'the-jndi-name'"));
        }
    }
}
