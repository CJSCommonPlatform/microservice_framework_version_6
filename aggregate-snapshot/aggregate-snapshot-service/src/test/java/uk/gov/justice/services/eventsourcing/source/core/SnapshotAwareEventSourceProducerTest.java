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

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareEventSourceProducerTest {


    @Mock
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Mock
    private SnapshotAwareEventSourceFactory snapshotAwareEventSourceFactory;

    @InjectMocks
    private SnapshotAwareEventSourceProducer snapshotAwareEventSourceProducer;

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
        when(snapshotAwareEventSourceFactory.create(eventSourceDefinition.getLocation().getDataSource().get(), eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(snapshotAwareEventSourceProducer.eventSource(), is(jdbcBasedEventSource));
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
        when(snapshotAwareEventSourceFactory.create(eventSourceDefinition.getLocation().getDataSource().get(), eventSourceDefinition.getName())).thenReturn(jdbcBasedEventSource);

        assertThat(snapshotAwareEventSourceProducer.eventSource(), is(jdbcBasedEventSource));


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
        when(eventSourceDefinition.getName()).thenReturn(eventSourceName);
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(of(dataSource));
        when(snapshotAwareEventSourceFactory.create(dataSource, eventSourceName)).thenReturn(jdbcBasedEventSource);

        assertThat(snapshotAwareEventSourceProducer.eventSource(injectionPoint), is(jdbcBasedEventSource));
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
            snapshotAwareEventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("Failed to find EventSource named 'my-event-source' in event-sources.yaml"));
        }

        verifyZeroInteractions(snapshotAwareEventSourceFactory);
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
        when(eventSourceDefinition.getName()).thenReturn(eventSourceName);
        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(location.getDataSource()).thenReturn(empty());
        when(eventSourceDefinition.getName()).thenReturn(dataSourceName);

        try {
            snapshotAwareEventSourceProducer.eventSource(injectionPoint);
            fail();
        } catch (final CreationException expected) {
            assertThat(expected.getMessage(), is("No DataSource specified for EventSource 'my-data-source' specified in event-sources.yaml"));
        }

        verifyZeroInteractions(snapshotAwareEventSourceFactory);
    }
}
