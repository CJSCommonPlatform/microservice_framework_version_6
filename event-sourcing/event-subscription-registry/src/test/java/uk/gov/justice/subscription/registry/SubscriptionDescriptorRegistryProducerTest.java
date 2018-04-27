package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorBuilder.subscriptionDescriptor;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorRegistryProducerTest {

    @Mock
    private YamlFileFinder yamlFileFinder;

    @Mock
    private SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    @InjectMocks
    private SubscriptionDescriptorRegistryProducer subscriptionRegistryProducer;

    @Test
    public void shouldCreateARegistryOfAllSubscriptionsFromTheClasspath() throws Exception {

        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final Path path_1 = mock(Path.class);
        final Path path_2 = mock(Path.class);

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(eventListener)
                .build();
        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(eventProcessor)
                .build();

        final List<Path> pathList = asList(path_1, path_2);

        when(yamlFileFinder.getSubscriptionDescriptorPaths()).thenReturn(pathList);
        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionDescriptor_1, subscriptionDescriptor_2));

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = subscriptionRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionDescriptorRegistry, is(notNullValue()));

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(eventListener), is(of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(eventProcessor), is(of(subscriptionDescriptor_2)));
    }
}
