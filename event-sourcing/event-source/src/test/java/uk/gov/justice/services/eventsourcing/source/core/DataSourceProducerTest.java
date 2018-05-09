package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceRegistry;

import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceProducerTest {

    @Mock
    InitialContext initialContext;

    @Mock
    EventSourceNameExtractor eventSourceNameExtractor;

    @Mock
    EventSourceRegistry eventSourceRegistry;

    @InjectMocks
    private DataSourceProducer dataSourceProducer;

    @Test
    public void shouldLookupTheDataSourceUsingTheNameFoundInTheSubscriptionYaml() throws Exception {

        final String eventSourceName = "eventSourceName";
        final String dataSourceName = "dataSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final uk.gov.justice.subscription.domain.eventsource.EventSource eventSource
                = mock(uk.gov.justice.subscription.domain.eventsource.EventSource.class);
        final Location location = mock(Location.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(eventSourceName);
        when(eventSourceRegistry.getEventSourceFor(eventSourceName)).thenReturn(of(eventSource));
        when(eventSource.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(of(dataSourceName));
        when(initialContext.lookup(dataSourceName)).thenReturn(dataSource);

        final Optional<DataSource> dataSourceOptional = dataSourceProducer.dataSource(injectionPoint);

        assertThat(dataSourceOptional.isPresent(), is(true));
    }

    @Test
    public void shouldReturnEmptyIfTheLocationFoundInTheSubscriptionYamlDoesNotHaveADataSourceName() throws Exception {

        final String eventSourceName = "eventSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final uk.gov.justice.subscription.domain.eventsource.EventSource eventSource
                = mock(uk.gov.justice.subscription.domain.eventsource.EventSource.class);
        final Location location = mock(Location.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(eventSourceName);
        when(eventSourceRegistry.getEventSourceFor(eventSourceName)).thenReturn(of(eventSource));
        when(eventSource.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(empty());

        assertThat(dataSourceProducer.dataSource(injectionPoint).isPresent(), is(false));

        verifyZeroInteractions(initialContext);
    }

    @Test
    public void shouldThrowADataSourceProducerExceptionIfNoEventSourceFoundInTheRegistry() throws Exception {


        final String eventSourceName = "eventSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(eventSourceName);
        when(eventSourceRegistry.getEventSourceFor(eventSourceName)).thenReturn(empty());

        try {
            dataSourceProducer.dataSource(injectionPoint);
            fail();
        } catch (final DataSourceProducerException expected) {
            assertThat(expected.getMessage(), is("Expected Event Source of name: eventSourceName not found, please check event-sources.yaml"));
        }
    }

    @Test
    public void shouldThrowADataSourceProducerExceptionIfLookingUpTheDataSourceFails() throws Exception {

        final NamingException namingException = new NamingException("Ooops");

        final String eventSourceName = "eventSourceName";
        final String dataSourceName = "dataSourceName";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final uk.gov.justice.subscription.domain.eventsource.EventSource eventSource
                = mock(uk.gov.justice.subscription.domain.eventsource.EventSource.class);
        final Location location = mock(Location.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventSourceNameExtractor.getEventSourceNameFromQualifier(injectionPoint)).thenReturn(eventSourceName);
        when(eventSourceRegistry.getEventSourceFor(eventSourceName)).thenReturn(of(eventSource));
        when(eventSource.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(of(dataSourceName));
        when(initialContext.lookup(dataSourceName)).thenThrow(namingException);

        try {
            dataSourceProducer.dataSource(injectionPoint);
            fail();
        } catch (final DataSourceProducerException expected) {
            assertThat(expected.getCause(), is(namingException));
            assertThat(expected.getMessage(), is("Data Source not found for the provided event source name: eventSourceName"));
        }
    }
}





