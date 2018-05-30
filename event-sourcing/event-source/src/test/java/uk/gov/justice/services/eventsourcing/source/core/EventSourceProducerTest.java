package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.EventSourceDefinitionBuilder.eventSourceDefinition;

import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import java.util.Optional;

import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceProducerTest {

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Mock
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @Mock
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @InjectMocks
    private EventSourceProducer eventSourceProducer;


    @Test
    public void shouldCreateDefaultEventSourceDefinitionWhenEventSourceNameIsEmpty() throws Exception {
        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefaultEventSource(true)
                .withLocation(new Location("", "", Optional.of("dataSource")))
                .build();
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);

        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);
        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn("");
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcEventSourceFactory.create(eventSourceDefinition.getLocation().getDataSource().get(), eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldCreateEventSourceUsingTheJNDINameInjectedByTheContainer() throws Exception {

        final EventSourceDefinition eventSourceDefinition = eventSourceDefinition()
                .withName("defaultEventSource")
                .withDefaultEventSource(true)
                .withLocation(new Location("", "", Optional.of("dataSource")))
                .build();

        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);

        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(jdbcEventSourceFactory.create(eventSourceDefinition.getLocation().getDataSource().get(), eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldCreateAnEventSourceUsingTheEventSourceNameAnnotation() throws Exception {

        final String eventSourceName = "eventSourceName";
        final String dataSource = "my-data-source";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);
        final JdbcBasedEventSource jdbcBasedEventSource = mock(JdbcBasedEventSource.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(eventSourceDefinition);
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(of(dataSource));
        when(jdbcEventSourceFactory.create(dataSource, eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(eventSourceProducer.eventSource(injectionPoint), is(jdbcBasedEventSource));
    }

    @Test
    public void shouldFailIfNoEventSourceFoundInTheEventSourceRegistry() throws Exception {

        final String eventSourceName = "my-event-source";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(null);

        try {
            eventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("Failed to find EventSource named 'my-event-source' in event-sources.yaml"));
        }

        verifyZeroInteractions(jdbcEventSourceFactory);
    }

    @Test
    public void shouldFailIfNoDataSourceNameFoundInEventSourcesYaml() throws Exception {

        final String eventSourceName = "eventSourceName";
        final String dataSourceName = "my-data-source";

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final EventSourceName eventSourceNameAnnotation = mock(EventSourceName.class);
        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);

        when(qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class)).thenReturn(eventSourceNameAnnotation);
        when(eventSourceNameAnnotation.value()).thenReturn(eventSourceName);
        when(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName)).thenReturn(eventSourceDefinition);
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(empty());
        when(eventSourceDefinition.getName()).thenReturn(dataSourceName);

        try {
            eventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("No DataSource specified for EventSource 'my-data-source' specified in event-sources.yaml"));
        }

        verifyZeroInteractions(jdbcEventSourceFactory);
    }
}


