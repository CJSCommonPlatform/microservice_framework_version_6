package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DataSourceProviderTest {

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
}
