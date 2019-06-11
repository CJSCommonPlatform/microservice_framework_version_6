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
public class SystemJdbcDataSourceProviderTest {

    @Mock
    private InitialContext initialContext;

    @Mock
    private SystemDataSourceNameProvider systemDataSourceNameProvider;

    @InjectMocks
    private SystemJdbcDataSourceProvider systemJdbcDataSourceProvider;

    @Test
    public void shouldLookUpTheDataSourceUsingTheCorrectJndiName() throws Exception {

        final String dataSourceName = "java:/app/framework/DS.system";
        final DataSource dataSource = mock(DataSource.class);

        when(systemDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenReturn(dataSource);

        assertThat(systemJdbcDataSourceProvider.getDataSource(), is(dataSource));
    }

    @Test
    public void shouldThrowExceptionIfTheLookupOfTheDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException();
        final String dataSourceName = "java:/app/framework/DS.system";

        when(systemDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenThrow(namingException);

        try {
            systemJdbcDataSourceProvider.getDataSource();
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup System DataSource using JNDI name 'java:/app/framework/DS.system'"));
        }
    }

    @Test
    public void shouldOnlyLookupTheDataSourceOnce() throws Exception {

        final String dataSourceName = "java:/app/framework/DS.system";
        final DataSource dataSource = mock(DataSource.class);

        when(systemDataSourceNameProvider.getDataSourceName()).thenReturn(dataSourceName);

        when(initialContext.lookup(dataSourceName)).thenReturn(dataSource);

       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();
       systemJdbcDataSourceProvider.getDataSource();

       verify(systemDataSourceNameProvider, times(1)).getDataSourceName();
    }
}
