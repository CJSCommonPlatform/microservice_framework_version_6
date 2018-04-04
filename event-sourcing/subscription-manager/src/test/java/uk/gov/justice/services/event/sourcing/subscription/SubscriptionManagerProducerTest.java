package uk.gov.justice.services.event.sourcing.subscription;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.cdi.SubscriptionName;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.Subscription;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerProducerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Instance<EventSource> eventsourceInstance;

    @Mock
    private SubscriptionDescriptorRegistry subscriptionDescriptorRegistry;

    @Mock
    private SubscriptionNameAnnotationExtractor subscriptionNameAnnotationExtractor;

    @InjectMocks
    private SubscriptionManagerProducer subscriptionManagerProducer;

    @Test
    public void shouldCreateSubscriptionManagersOnStartUp() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        final SubscriptionName subscriptionName = mock(SubscriptionName.class);
        final EventSource eventSource = mock(EventSource.class);
        final Subscription subscription = mock(Subscription.class);

        when(subscriptionNameAnnotationExtractor.getFrom(injectionPoint)).thenReturn(subscriptionName);
        when(eventsourceInstance.select(subscriptionName).get()).thenReturn(eventSource);
        when(subscriptionDescriptorRegistry.getSubscription(subscriptionName.value())).thenReturn(subscription);

        final SubscriptionManager subscriptionManager = subscriptionManagerProducer.subscriptionManager(injectionPoint);

        assertThat(subscriptionManager.getEventSource(), is(eventSource));
        assertThat(subscriptionManager.getSubscription(), is(subscription));
    }

    @Test
    public void shouldThrowASubscriptioManagerProducerExceptionIfTheEventSourceInstanceReturnsANull() throws Exception {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        final SubscriptionName subscriptionName = mock(SubscriptionName.class);

        when(subscriptionNameAnnotationExtractor.getFrom(injectionPoint)).thenReturn(subscriptionName);
        when(eventsourceInstance.select(subscriptionName)).thenReturn(null);
        when(subscriptionName.value()).thenReturn("my-subscription");

        try {
            subscriptionManagerProducer.subscriptionManager(injectionPoint);
            fail();
        } catch (final SubscriptionManagerProducerException expected) {
            assertThat(expected.getMessage(), is("Failed to find instance of event souce with Qualifier 'my-subscription'"));
        }
    }
}
