package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorBuilder.subscriptionDescriptor;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionDescriptorRegistryProducerTest {

    @Mock
    private Logger logger;

    @Mock
    private YamlFileFinder yamlFileFinder;

    @Mock
    private SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    @InjectMocks
    private SubscriptionDescriptorRegistryProducer subscriptionRegistryProducer;

    @Test
    public void shouldCreateRegistryOfAllSubscriptionsFromTheClasspath() throws Exception {

        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(eventListener)
                .build();
        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(eventProcessor)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionDescriptorPaths()).thenReturn(pathList);
        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionDescriptor_1, subscriptionDescriptor_2));

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = subscriptionRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionDescriptorRegistry, is(notNullValue()));

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(eventListener), is(of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(eventProcessor), is(of(subscriptionDescriptor_2)));
    }

    @Test
    public void shouldCreateSingleRegistryAndReturnSameInstance() throws Exception {
        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(eventListener)
                .build();
        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(eventProcessor)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionDescriptorPaths()).thenReturn(pathList);
        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionDescriptor_1, subscriptionDescriptor_2));

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry_1 = subscriptionRegistryProducer.subscriptionDescriptorRegistry();
        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry_2 = subscriptionRegistryProducer.subscriptionDescriptorRegistry();

        assertThat(subscriptionDescriptorRegistry_1, is(sameInstance(subscriptionDescriptorRegistry_2)));
    }

    @Test
    public void shouldLogRegisteredSubscriptions() throws Exception {

        final String eventListener = "EVENT_LISTENER";
        final String eventProcessor = "EVENT_PROCESSOR";

        final URL url_1 = new URL("file:/test-1");
        final URL url_2 = new URL("file:/test-2");

        final Subscription subscription_1 = subscription()
                .withName("Subscription_1")
                .build();

        final Subscription subscription_2 = subscription()
                .withName("Subscription_2")
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(eventListener)
                .withSubscription(subscription_1)
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(eventProcessor)
                .withSubscription(subscription_2)
                .build();

        final List<URL> pathList = asList(url_1, url_2);

        when(yamlFileFinder.getSubscriptionDescriptorPaths()).thenReturn(pathList);
        when(subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(pathList)).thenReturn(Stream.of(subscriptionDescriptor_1, subscriptionDescriptor_2));

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = subscriptionRegistryProducer.subscriptionDescriptorRegistry();

        verify(logger).info("Subscription name in registry : Subscription_1");
        verify(logger).info("Subscription name in registry : Subscription_2");
    }

    @Test
    public void shouldThrowExceptionIfIOExceptionOccursWhenFindingSubscriptionDescriptorResourcesOnTheClasspath() throws Exception {

        when(yamlFileFinder.getSubscriptionDescriptorPaths()).thenThrow(new IOException());

        try {
            subscriptionRegistryProducer.subscriptionDescriptorRegistry();
            fail();
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(RegistryException.class)));
            assertThat(e.getMessage(), is("Failed to find yaml/subscription-descriptor.yaml resources on the classpath"));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }
}
