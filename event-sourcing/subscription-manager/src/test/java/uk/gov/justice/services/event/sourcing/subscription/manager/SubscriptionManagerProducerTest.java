package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;

import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionsDescriptorsRegistry;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionManagerProducerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Instance<EventSource> eventSourceInstance;

    @Mock
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry;

    @Mock
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Mock
    private EventBufferSelector eventBufferSelector;

    @Mock
    private Logger logger;

    @InjectMocks
    private SubscriptionManagerProducer subscriptionManagerProducer;

    @Test
    public void shouldCreateSubscriptionManagersOnStartUp() {

        final InjectionPoint injectionPoint = mock(InjectionPoint.class);

        final SubscriptionName subscriptionName = mock(SubscriptionName.class);
        final EventBufferService eventBufferService = mock(EventBufferService.class);
        final EventSource eventSource = mock(EventSource.class);
        final InterceptorChainProcessor interceptorChainProcessor = mock(InterceptorChainProcessor.class);

        final Subscription subscription = subscription()
                .withEventSourceName("eventSourceName")
                .withName("subscriptionName")
                .build();

        when(subscriptionDescriptorRegistry.getSubscriptionFor(subscription.getName())).thenReturn(subscription);
        when(subscriptionDescriptorRegistry.findComponentNameBy(subscription.getName())).thenReturn(EVENT_LISTENER);
        when(qualifierAnnotationExtractor.getFrom(injectionPoint, SubscriptionName.class)).thenReturn(subscriptionName);
        when(interceptorChainProcessorProducer.produceLocalProcessor(EVENT_LISTENER)).thenReturn(interceptorChainProcessor);
        when(subscriptionName.value()).thenReturn("subscriptionName");
        when(subscriptionDescriptorRegistry.getSubscriptionFor(subscriptionName.value())).thenReturn(subscription);

        when(eventSourceInstance.select(any(EventSourceNameQualifier.class)).get()).thenReturn(eventSource);
        when(eventBufferSelector.selectFor(EVENT_LISTENER)).thenReturn(of(eventBufferService));

        final SubscriptionManager subscriptionManager = subscriptionManagerProducer.subscriptionManager(injectionPoint);

        assertThat(subscriptionManager, is(instanceOf(DefaultSubscriptionManager.class)));
        verify(logger).debug("Creating subscription manager for subscription name: " + subscriptionName.value());
    }
}
