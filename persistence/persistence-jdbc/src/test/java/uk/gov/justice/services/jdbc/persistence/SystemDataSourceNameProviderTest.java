package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemDataSourceNameProviderTest {

    @Mock
    private JndiAppNameProvider jndiAppNameProvider;

    @InjectMocks
    private SystemDataSourceNameProvider systemDataSourceNameProvider;

    @Test
    public void shouldCorrectlyGenerateTheCorrectDataSourceNameBasedOnTheAppName() throws Exception {

        when(jndiAppNameProvider.getAppName()).thenReturn("framework");

        assertThat(systemDataSourceNameProvider.getDataSourceName(), is("java:/app/framework/DS.system"));
    }

    @Test
    public void shouldIgnoreAnyPartOfTheAppNameAfterAHyphen() throws Exception {

        when(jndiAppNameProvider.getAppName()).thenReturn("framework-context-with-hyphens");

        assertThat(systemDataSourceNameProvider.getDataSourceName(), is("java:/app/framework/DS.system"));
    }

    @Test
    public void shouldOnlyGenerateTheNameOnce() throws Exception {

        when(jndiAppNameProvider.getAppName()).thenReturn("framework");

        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();
        systemDataSourceNameProvider.getDataSourceName();

        verify(jndiAppNameProvider, times(1)).getAppName();
    }
}
