package uk.gov.justice.services.jdbc.persistence;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class JdbcDataSourceProviderTest {

    @Mock
    private InitialContext initialContext;

    @InjectMocks
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Test
    public void shouldLookupTheDataSourceInTheInitialContextAndCache() throws Exception {

        final String jndiName = "the-jndi-name";

        final DataSource dataSource = mock(DataSource.class);

        when(initialContext.lookup(jndiName)).thenReturn(dataSource);

        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
        assertThat(jdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));

        verify(initialContext, times(1)).lookup(jndiName);
    }

    @Test
    public void shouldThrowAJdbcRepositoryExceptionIfTheLookupOfTheDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException("Ooops");

        final String jndiName = "the-jndi-name";

        when(initialContext.lookup(jndiName)).thenThrow(namingException);

        try {
            jdbcDataSourceProvider.getDataSource(jndiName);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup DataSource using jndi name 'the-jndi-name'"));
        }
    }
}
