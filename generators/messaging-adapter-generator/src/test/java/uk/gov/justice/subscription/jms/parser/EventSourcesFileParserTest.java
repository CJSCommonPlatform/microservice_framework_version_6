package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourcesFileParserTest {

    @Mock
    private EventSourcesParser eventSourcesParser;

    @Mock
    private PathToUrlResolver pathToUrlResolver;

    @InjectMocks
    private EventSourcesFileParser eventSourcesFileParser;

    @Test
    public void shouldReturnEventSources() throws Exception {
        final EventSourceDefinition eventSource = mock(EventSourceDefinition.class);
        final Path baseDir = Paths.get("/yaml");

        final Path eventSourcePath = Paths.get("event-sources.yaml");
        final Path subscriptionDescriptorPath = Paths.get("subscription-descriptor.yaml");
        final List<Path> pathList = asList(eventSourcePath, subscriptionDescriptorPath);

        final URL eventSourceUrl = baseDir.resolve(eventSourcePath).toUri().toURL();
        final List<URL> urlList = singletonList(eventSourceUrl);

        when(pathToUrlResolver.resolveToUrl(baseDir, eventSourcePath)).thenReturn(eventSourceUrl);
        when(eventSourcesParser.getEventSourcesFrom(urlList)).thenReturn(Stream.of(eventSource));

        final List<EventSourceDefinition> expectedEventSourceDefinitions = eventSourcesFileParser.getEventSources(baseDir, pathList);

        assertThat(expectedEventSourceDefinitions.size(), is(1));
        assertThat(expectedEventSourceDefinitions.get(0), is(eventSource));
    }

    @Test
    public void shouldThrowExceptionIfNoEventSourcesPresent() {
        final Path baseDir = mock(Path.class);

        try {
            eventSourcesFileParser.getEventSources(baseDir, emptyList());
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(FileParserException.class)));
            assertThat(e.getMessage(), is("No event-sources.yaml files found!"));
        }
    }
}