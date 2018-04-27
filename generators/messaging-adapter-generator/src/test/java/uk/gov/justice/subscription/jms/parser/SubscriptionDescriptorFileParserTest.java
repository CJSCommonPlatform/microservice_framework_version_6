package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorFileParserTest {

    @Mock
    private SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    @InjectMocks
    private SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;

    @Test
    public void shouldCreateSubscriptionWrappers() throws Exception {

        final EventSource eventSourceDefinition = mock(EventSource.class);
        final SubscriptionDescriptor subscriptionDescriptor = mock(SubscriptionDescriptor.class);

        final Path baseDir = mock(Path.class);
        final Path subscriptionPath = mock(Path.class);
        final Path eventSourcePath = mock(Path.class);
        final Path resolvedPath = mock(Path.class);

        final Collection<Path> paths = asList(subscriptionPath, eventSourcePath);

        when(subscriptionPath.endsWith("event-sources.yaml")).thenReturn(false);
        when(eventSourcePath.endsWith("event-sources.yaml")).thenReturn(true);
        when(baseDir.resolve(subscriptionPath)).thenReturn(resolvedPath);

        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(singletonList(resolvedPath))).thenReturn(Stream.of(subscriptionDescriptor));
        when(eventSourceDefinition.getName()).thenReturn("eventSourceName");

        final List<SubscriptionWrapper> subscriptionWrappers = subscriptionDescriptorFileParser.getSubscriptionWrappers(baseDir, paths, singletonList(eventSourceDefinition));

        assertThat(subscriptionWrappers.size(), is(1));
        assertThat(subscriptionWrappers.get(0).getSubscriptionDescriptor(), is(subscriptionDescriptor));
        assertThat(subscriptionWrappers.get(0).getEventSourceByName("eventSourceName"), is(eventSourceDefinition));
    }
}