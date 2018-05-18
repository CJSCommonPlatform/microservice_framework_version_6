package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.EventSourceBuilder.eventsource;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceDefinitionRegistryProducerTest {

    @Mock
    private YamlFileFinder yamlFileFinder;

    @Mock
    private EventSourcesParser eventSourcesParser;

    @InjectMocks
    private EventSourceDefinitionRegistryProducer eventSourceDefinitionRegistryProducer;

    @Test
    public void shouldCreateARegistryOfAllEventSourceDefinitionsFromTheClasspath() throws Exception {

        final String event_source_name_1 = "event_source_name_1";
        final String event_source_name_2 = "event_source_name_2";

        final URL url_1 = new URL("file:/test");
        final URL url_2 = new URL("file:/test");

        final EventSourceDefinition eventSourceDefinition1 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_1).build();

        final EventSourceDefinition eventSourceDefinition2 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_2).build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getEventSourcesPaths()).thenReturn(pathList);
        when(eventSourcesParser.eventSourcesFrom(pathList)).thenReturn(Stream.of(eventSourceDefinition1, eventSourceDefinition2));

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry = eventSourceDefinitionRegistryProducer.getEventSourceDefinitionRegistry();

        assertThat(eventSourceDefinitionRegistry, is(notNullValue()));

        assertThat(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(event_source_name_1), is(of(eventSourceDefinition1)));
        assertThat(eventSourceDefinitionRegistry.getEventSourceDefinitionFor(event_source_name_2), is(of(eventSourceDefinition2)));
    }

    @Test
    public void shouldCreateSingleRegistryAndReturnSameInstance() throws Exception {

        final String event_source_name_1 = "event_source_name_1";
        final String event_source_name_2 = "event_source_name_2";

        final URL url_1 = new URL("file:/test");
        final URL url_2 = new URL("file:/test");

        final EventSourceDefinition eventSourceDefinition1 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_1).build();

        final EventSourceDefinition eventSourceDefinition2 = eventsource()
                .withLocation(mock(Location.class))
                .withName(event_source_name_2).build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getEventSourcesPaths()).thenReturn(pathList);
        when(eventSourcesParser.eventSourcesFrom(pathList)).thenReturn(Stream.of(eventSourceDefinition1, eventSourceDefinition2));

        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry_1 = eventSourceDefinitionRegistryProducer.getEventSourceDefinitionRegistry();
        final EventSourceDefinitionRegistry eventSourceDefinitionRegistry_2 = eventSourceDefinitionRegistryProducer.getEventSourceDefinitionRegistry();

        assertThat(eventSourceDefinitionRegistry_1, is(sameInstance(eventSourceDefinitionRegistry_2)));
    }

    @Test
    public void shouldThrowExceptionIfIOExceptionOccursWhenFindingEventSourcesOnTheClasspath() throws Exception {

        when(yamlFileFinder.getEventSourcesPaths()).thenThrow(new IOException());

        try {
            eventSourceDefinitionRegistryProducer.getEventSourceDefinitionRegistry();
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(RegistryException.class)));
            assertThat(e.getMessage(), is("Failed to find yaml/event-sources.yaml resources on the classpath"));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }
}