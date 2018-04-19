package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.nio.file.Path;
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
    EventSourcesParser eventSourcesParser;

    @InjectMocks
    private EventSourcesFileParser eventSourcesFileParser;

    @Test
    public void shouldReturnEventSources() {
        final Path baseDir = mock(Path.class);
        final Path resolvedPath = mock(Path.class);

        final Path eventSourcePath = mock(Path.class);
        final Path subscriptionDescriptorPath = mock(Path.class);
        final List<Path> pathList = asList(eventSourcePath, subscriptionDescriptorPath);

        when(subscriptionDescriptorPath.endsWith("event-sources.yaml")).thenReturn(false);
        when(eventSourcePath.endsWith("event-sources.yaml")).thenReturn(true);

        when(baseDir.resolve(eventSourcePath)).thenReturn(resolvedPath);
        when(eventSourcesParser.getEventSourcesFrom(singletonList(resolvedPath))).thenReturn(Stream.of(mock(EventSource.class)));

        final List<EventSource> expectedEventSourceDefinitions = eventSourcesFileParser.getEventSources(baseDir, pathList);

        assertThat(expectedEventSourceDefinitions.size(), is(1));
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