package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubscriptionManagerTest {

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private Subscription subscription;

    @Mock
    private EventSource eventSource;

    @Mock
    private EventBufferService eventBufferService;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<InterceptorContext> interceptorContextArgumentCaptor;

    @Test
    public void shouldProcessWithEventBuffer() throws Exception {

        final Optional<EventBufferService> eventBufferServiceOptional = of(eventBufferService);
        final DefaultSubscriptionManager defaultSubscriptionManager = new DefaultSubscriptionManager(
                subscription,
                eventSource,
                interceptorChainProcessor,
                eventBufferServiceOptional,
                logger
        );

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope existingJsonEnvelope = mock(JsonEnvelope.class);

        final Stream<JsonEnvelope> envelopeStream = Stream.of(incomingJsonEnvelope, existingJsonEnvelope);

        when(eventBufferService.currentOrderedEventsWith(incomingJsonEnvelope)).thenReturn(envelopeStream);

        defaultSubscriptionManager.process(incomingJsonEnvelope);

        verify(interceptorChainProcessor, times(2)).process(interceptorContextArgumentCaptor.capture());

        final List<InterceptorContext> interceptorContexts = interceptorContextArgumentCaptor.getAllValues();

        assertThat(interceptorContexts.size(), is(2));

        assertThat(interceptorContexts.get(0).inputEnvelope(), is(incomingJsonEnvelope));
        assertThat(interceptorContexts.get(1).inputEnvelope(), is(existingJsonEnvelope));
    }

    @Test
    public void shouldProcessWithoutEventBuffer() throws Exception {

        final Optional<EventBufferService> eventBufferServiceOptional = empty();
        final DefaultSubscriptionManager defaultSubscriptionManager = new DefaultSubscriptionManager(
                subscription,
                eventSource,
                interceptorChainProcessor,
                eventBufferServiceOptional,
                logger
        );

        final JsonEnvelope incomingJsonEnvelope = mock(JsonEnvelope.class);

        defaultSubscriptionManager.process(incomingJsonEnvelope);

        verify(interceptorChainProcessor).process(interceptorContextArgumentCaptor.capture());

        final InterceptorContext interceptorContext = interceptorContextArgumentCaptor.getValue();

        assertThat(interceptorContext.inputEnvelope(), is(incomingJsonEnvelope));

        verifyZeroInteractions(eventBufferService);
    }

    @Test
    public void shouldStartSubscription() {

        final DefaultSubscriptionManager defaultSubscriptionManager = new DefaultSubscriptionManager(
                subscription,
                eventSource,
                interceptorChainProcessor,
                empty(),
                logger
        );

        when(subscription.getName()).thenReturn("subscription");
        when(subscription.getEventSourceName()).thenReturn("eventSource");

        defaultSubscriptionManager.startSubscription();

        verify(logger).debug(format("Starting subscription: %s for event source: %s", subscription, eventSource));
    }
}
