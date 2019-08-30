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
public class DefaultJdbcDataSourceProviderTest {

    @Mock
    private InitialContext initialContext;

    @InjectMocks
    private DefaultJdbcDataSourceProvider defaultJdbcDataSourceProvider;

    @Test
    public void shouldLookupDataSourceByName() throws Exception {

        final String jndiName = "jndiName";

        final DataSource dataSource = mock(DataSource.class);

        when(initialContext.lookup(jndiName)).thenReturn(dataSource);

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName), is(dataSource));
    }

    @Test
    public void shouldCacheDataSourceOnceLookedUp() throws Exception {

        final String jndiName = "jndiName";

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
    public void shouldHandleMutipleDataSources() throws Exception {

        final String jndiName_1 = "jndiName_1";
        final String jndiName_2 = "jndiName_2";
        final String jndiName_3 = "jndiName_3";

        final DataSource dataSource_1 = mock(DataSource.class);
        final DataSource dataSource_2 = mock(DataSource.class);
        final DataSource dataSource_3 = mock(DataSource.class);

        when(initialContext.lookup(jndiName_1)).thenReturn(dataSource_1);
        when(initialContext.lookup(jndiName_2)).thenReturn(dataSource_2);
        when(initialContext.lookup(jndiName_3)).thenReturn(dataSource_3);

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_1), is(dataSource_1));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_2), is(dataSource_2));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_3), is(dataSource_3));

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_1), is(dataSource_1));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_2), is(dataSource_2));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_3), is(dataSource_3));

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_1), is(dataSource_1));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_2), is(dataSource_2));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_3), is(dataSource_3));

        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_1), is(dataSource_1));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_2), is(dataSource_2));
        assertThat(defaultJdbcDataSourceProvider.getDataSource(jndiName_3), is(dataSource_3));

        verify(initialContext, times(1)).lookup(jndiName_1);
        verify(initialContext, times(1)).lookup(jndiName_2);
        verify(initialContext, times(1)).lookup(jndiName_3);
    }

    @Test
    public void shouldFailIfLookingUpDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException("Ooops");
        final String jndiName = "jndiName";

        when(initialContext.lookup(jndiName)).thenThrow(namingException);

        try {
            defaultJdbcDataSourceProvider.getDataSource(jndiName);
            fail();
        } catch (final JdbcRepositoryException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Failed to lookup DataSource using jndi name 'jndiName'"));
        }
    }
}
