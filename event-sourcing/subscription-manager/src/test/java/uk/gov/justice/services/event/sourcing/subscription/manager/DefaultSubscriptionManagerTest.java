package uk.gov.justice.services.event.sourcing.subscription.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSubscriptionManagerTest {

    @Mock
    private Logger logger;

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Mock
    private Subscription subscription;

    @Mock
    private EventSource eventSource;

    @InjectMocks
    private DefaultSubscriptionManager defaultSubscriptionManager;

    @Captor
    private ArgumentCaptor<InterceptorContext> interceptorContextArgumentCaptor;

    @Test
    public void shouldProcessJsonEnvelope() {
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        defaultSubscriptionManager.process(jsonEnvelope);

        verify(interceptorChainProcessor).process(interceptorContextArgumentCaptor.capture());

        final InterceptorContext interceptorContext = interceptorContextArgumentCaptor.getValue();
        assertThat(interceptorContext.inputEnvelope(), is(jsonEnvelope));
    }

    @Test
    public void shouldStartSubscription() {

        defaultSubscriptionManager.logger = this.logger;

        when(subscription.getName()).thenReturn("subscription");
        when(subscription.getEventSourceName()).thenReturn("eventSource");

        defaultSubscriptionManager.startSubscription();

        verify(logger).debug(format("Starting subscription: %s for event source: %s", subscription, eventSource));
    }
}
