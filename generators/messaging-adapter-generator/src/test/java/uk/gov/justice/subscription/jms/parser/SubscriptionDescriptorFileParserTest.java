package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Mock
    private PathToUrlResolver pathToUrlResolver;

    @InjectMocks
    private SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;

    @Test
    public void shouldCreateSubscriptionWrappers() throws Exception {

        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition = mock(SubscriptionDescriptorDefinition.class);

        final Path baseDir = Paths.get("/yaml");
        final Path eventSourcePath = Paths.get("event-sources.yaml");
        final Path subscriptionDescriptorPath = Paths.get("subscription-descriptor.yaml");
        final Collection<Path> paths = asList(subscriptionDescriptorPath, eventSourcePath);

        final URL subscriptionDescriptorUrl = new URL("file:/subscription-descriptor.yaml");
        final List<URL> urlList = singletonList(subscriptionDescriptorUrl);

        when(pathToUrlResolver.resolveToUrl(baseDir, subscriptionDescriptorPath)).thenReturn(subscriptionDescriptorUrl);
        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(urlList)).thenReturn(Stream.of(subscriptionDescriptorDefinition));
        when(eventSourceDefinition.getName()).thenReturn("eventSourceName");

        final List<SubscriptionWrapper> subscriptionWrappers = subscriptionDescriptorFileParser.getSubscriptionWrappers(baseDir, paths, singletonList(eventSourceDefinition));

        assertThat(subscriptionWrappers.size(), is(1));
        assertThat(subscriptionWrappers.get(0).getSubscriptionDescriptorDefinition(), is(subscriptionDescriptorDefinition));
        assertThat(subscriptionWrappers.get(0).getEventSourceByName("eventSourceName"), is(eventSourceDefinition));
    }
}