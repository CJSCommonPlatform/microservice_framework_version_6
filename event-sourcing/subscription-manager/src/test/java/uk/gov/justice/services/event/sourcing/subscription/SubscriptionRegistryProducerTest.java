package uk.gov.justice.services.event.sourcing.subscription;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;

import java.nio.file.Path;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRegistryProducerTest {

    @Mock
    SubscriptionDescriptorFileFinder subscriptionDescriptorFileFinder;

    @Mock
    SubscriptionDescriptorParser subscriptionDescriptorParser;

    @InjectMocks
    private SubscriptionRegistryProducer subscriptionRegistryProducer;

    @Test
    public void shouldCreateARegistryOfAllSubscriptionsFromTheClasspath() throws Exception {

        final Path path_1 = mock(Path.class);
        final Path path_2 = mock(Path.class);

        final SubscriptionDescriptor subscriptionDescriptor_1 = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptor subscriptionDescriptor_2 = mock(SubscriptionDescriptor.class);

        final String service_1 = "service_1";
        final String service_2 = "service_2";

        when(subscriptionDescriptorFileFinder.findOnClasspath()).thenReturn(asList(path_1,path_2));
        when(subscriptionDescriptorParser.read(path_1)).thenReturn(subscriptionDescriptor_1);
        when(subscriptionDescriptorParser.read(path_2)).thenReturn(subscriptionDescriptor_2);
        when(subscriptionDescriptor_1.getService()).thenReturn(service_1);
        when(subscriptionDescriptor_2.getService()).thenReturn(service_2);

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = subscriptionRegistryProducer.subscriptionDescriptorRegistry(
                mock(InjectionPoint.class)
        );

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(service_1), is(of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(service_2), is(of(subscriptionDescriptor_2)));
    }
}
