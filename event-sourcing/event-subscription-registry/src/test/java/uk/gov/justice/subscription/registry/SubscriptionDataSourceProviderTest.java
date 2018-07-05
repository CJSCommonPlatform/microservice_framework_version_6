package uk.gov.justice.subscription.registry;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDataSourceProviderTest {

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @InjectMocks
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Test
    public void shouldFindTheCorrectJndiNameAndUseItToCreateTheDataSourceAndCacheIt() throws Exception {

        final EventSourceDefinition defaultEventSourceDefinition = mock(EventSourceDefinition.class, RETURNS_DEEP_STUBS.get());
        final DataSource dataSource = mock(DataSource.class);

        final String jndiDatasource = "jndi datasource name";

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(defaultEventSourceDefinition);
        when(defaultEventSourceDefinition.getLocation().getDataSource()).thenReturn(of(jndiDatasource));

        when(jdbcDataSourceProvider.getDataSource(jndiDatasource)).thenReturn(dataSource);

        assertThat(subscriptionDataSourceProvider.getEventStoreDataSource(), is(dataSource));
        assertThat(subscriptionDataSourceProvider.getEventStoreDataSource(), is(dataSource));
        assertThat(subscriptionDataSourceProvider.getEventStoreDataSource(), is(dataSource));

        verify(jdbcDataSourceProvider, times(1)).getDataSource(jndiDatasource);
    }

    @Test
    public void shouldThrowRegistryExceptionIfNoDataSourceSpecifiedInYaml() throws Exception {

        final EventSourceDefinition defaultEventSourceDefinition = mock(EventSourceDefinition.class, RETURNS_DEEP_STUBS.get());

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(defaultEventSourceDefinition);
        when(defaultEventSourceDefinition.getLocation().getDataSource()).thenReturn(empty());

        try {
            subscriptionDataSourceProvider.getEventStoreDataSource();
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("No 'data_source' specified in 'event-sources.yaml'"));
        }
    }
}
