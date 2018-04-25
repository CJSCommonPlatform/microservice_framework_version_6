package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourcesFileParserTest {

    @Mock
    private YamlParser yamlParser;

    @Mock
    private YamlFileValidator yamlFileValidator;

    @InjectMocks
    private EventSourcesFileParser eventSourcesFileParser;

    @Test
    public void shouldReturnSingleEventSources() {
        final Path baseDir = mock(Path.class);
        final Path resolvedPath = mock(Path.class);

        final Path eventSourcePath = mock(Path.class);
        final Path subscriptionDescriptorPath = mock(Path.class);
        final List<Path> pathList = asList(eventSourcePath, subscriptionDescriptorPath);

        final EventSources eventSources = mock(EventSources.class);

        when(subscriptionDescriptorPath.endsWith("event-sources.yaml")).thenReturn(false);
        when(eventSourcePath.endsWith("event-sources.yaml")).thenReturn(true);

        when(baseDir.resolve(eventSourcePath)).thenReturn(resolvedPath);
        when(yamlParser.parseYamlFrom(resolvedPath, EventSources.class)).thenReturn(eventSources);


        final EventSources expectedEventSources = eventSourcesFileParser.getEventSources(baseDir, pathList);

        assertThat(expectedEventSources, is(eventSources));
    }

    @Test
    public void shouldThrowExceptionIfMoreThanOneEventSourcesPresent() {
        final Path baseDir = mock(Path.class);
        final Path resolvedPath = mock(Path.class);

        final Path eventSourcePath = mock(Path.class);
        final Path subscriptionDescriptorPath = mock(Path.class);
        final List<Path> pathList = asList(eventSourcePath, eventSourcePath, subscriptionDescriptorPath);

        final EventSources eventSources = mock(EventSources.class);

        when(subscriptionDescriptorPath.endsWith("event-sources.yaml")).thenReturn(false);
        when(eventSourcePath.endsWith("event-sources.yaml")).thenReturn(true);

        when(baseDir.resolve(eventSourcePath)).thenReturn(resolvedPath);
        when(yamlParser.parseYamlFrom(resolvedPath, EventSources.class)).thenReturn(eventSources);

        try {
            eventSourcesFileParser.getEventSources(baseDir, pathList);
            fail();
        } catch (final SubscriptionFileParserException e) {
            assertThat(e.getMessage(), is("More then one event-sources.yaml files found!"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoEventSourcesPresent() {
        final Path baseDir = mock(Path.class);

        try {
            eventSourcesFileParser.getEventSources(baseDir, emptyList());
            fail();
        } catch (final SubscriptionFileParserException e) {
            assertThat(e.getMessage(), is("No event-sources.yaml files found!"));
        }
    }
}