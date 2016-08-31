package uk.gov.justice.services.core.eventbuffer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.Interceptor;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.core.interceptor.Target;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventBufferInterceptorTest {

    private static final int EVENT_BUFFER_PRIORITY = 1000;

    @InjectMocks
    private EventBufferInterceptor eventBufferInterceptor;

    @Mock
    private JsonEnvelope envelope_1;

    @Mock
    private JsonEnvelope envelope_2;

    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private EventBufferService eventBufferService;

    private InterceptorChain interceptorChain;
    private TestTarget target;

    @Before
    public void setup() throws Exception {
        final Deque<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(eventBufferInterceptor);

        target = new TestTarget();
        interceptorChain = new InterceptorChain(interceptors, target);
    }

    @Test
    public void shouldCallEventBufferServiceAndProcessStreamOfMultipleEventsReturned() throws Exception {
        final InterceptorContext inputContext = interceptorContextWithInput(envelope_1, injectionPoint);
        final Stream<JsonEnvelope> envelopes = Stream.of(envelope_1, envelope_2);

        when(eventBufferService.currentOrderedEventsWith(envelope_1)).thenReturn(envelopes);

        final InterceptorContext resultContext = interceptorChain.processNext(inputContext);

        assertThat(resultContext.inputEnvelope(), is(envelope_1));
        assertThat(target.envelopesRecieved, contains(envelope_1, envelope_2));
    }

    @Test
    public void shouldCallEventBufferServiceAndProcessEmptyStreamReturned() throws Exception {
        final Stream<JsonEnvelope> envelopeStream = Stream.empty();
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope_1, injectionPoint);

        when(eventBufferService.currentOrderedEventsWith(envelope_1)).thenReturn(envelopeStream);

        final InterceptorContext resultContext = interceptorChain.processNext(interceptorContext);
        assertThat(resultContext, is(interceptorContext));
        assertThat(target.envelopesRecieved, empty());
    }

    @Test
    public void shouldReturnEventBufferPriority() throws Exception {
        assertThat(eventBufferInterceptor.priority(), is(EVENT_BUFFER_PRIORITY));
    }

    private static class TestTarget implements Target {

        private final List<JsonEnvelope> envelopesRecieved = new ArrayList<>();

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext) {
            envelopesRecieved.add(interceptorContext.inputEnvelope());
            return interceptorContext;
        }
    }
}