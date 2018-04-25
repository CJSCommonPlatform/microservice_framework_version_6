package uk.gov.justice.subscription.jms.parser;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorFileParserTest {

    @Mock
    private YamlParser yamlParser;

    @Mock
    private YamlFileValidator yamlFileValidator;

    @InjectMocks
    private SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;


    @Test
    public void shouldCreateSubscriptionWrappers() throws Exception {

        final EventSources eventSources = mock(EventSources.class);
        final Path baseDir = mock(Path.class);
        final Path subscriptionPath = mock(Path.class);
        final Path eventSourcePath = mock(Path.class);
        final Path resolvedPath = mock(Path.class);
        final SubscriptionDescriptor subscriptionDescriptor = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptorDef subscriptionDescriptorDef = mock(SubscriptionDescriptorDef.class);

        final Collection<Path> paths = asList(subscriptionPath, eventSourcePath);

        when(subscriptionPath.endsWith("event-sources.yaml")).thenReturn(false);
        when(eventSourcePath.endsWith("event-sources.yaml")).thenReturn(true);
        when(baseDir.resolve(subscriptionPath)).thenReturn(resolvedPath);
        when(yamlParser.parseYamlFrom(resolvedPath, SubscriptionDescriptorDef.class)).thenReturn(subscriptionDescriptorDef);
        when(subscriptionDescriptorDef.getSubscriptionDescriptor()).thenReturn(subscriptionDescriptor);
        when(eventSources.getEventSources()).thenReturn(asList(new EventSource("eventSourceName", mock(Location.class))));

        final List<SubscriptionWrapper> subscriptionWrappers = subscriptionDescriptorFileParser.getSubscriptionWrappers(baseDir, paths, eventSources);

        assertThat(subscriptionWrappers.size(), is(1));
        assertThat(subscriptionWrappers.get(0).getSubscriptionDescriptor() , is(subscriptionDescriptor));
        assertThat(subscriptionWrappers.get(0).getEventSourceByName("eventSourceName") , is(notNullValue()));
    }
}