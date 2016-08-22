package uk.gov.justice.services.core.eventbuffer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventBufferInterceptorTest {

    private static final int EVENT_BUFFER_PRIORITY = 1000;

    @Mock
    EventBufferService eventBufferService;

    @InjectMocks
    EventBufferInterceptor eventBufferInterceptor;

    @Mock
    private InterceptorChain interceptorChain;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProcessStreamOfBufferedEventsAndReturnTheFirstReturnedInterceptorContext() throws Exception {
        final JsonEnvelope envelope_1 = mock(JsonEnvelope.class);
        final JsonEnvelope envelope_2 = mock(JsonEnvelope.class);
        final InterceptorContext expectedContext = mock(InterceptorContext.class);

        final Stream<JsonEnvelope> envelopeStream = Stream.of(envelope_1, envelope_2);
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope_1, mock(InjectionPoint.class));

        when(eventBufferService.currentOrderedEventsWith(envelope_1)).thenReturn(envelopeStream);
        when(interceptorChain.processNext(any(Stream.class))).thenReturn(asList(expectedContext, mock(InterceptorContext.class)));

        final InterceptorContext resultContext = eventBufferInterceptor.process(interceptorContext, interceptorChain);
        assertThat(resultContext, is(expectedContext));

        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(interceptorChain).processNext(argumentCaptor.capture());

        final List<InterceptorContext> interceptorContexts = ((Stream<InterceptorContext>) argumentCaptor
                .getValue())
                .collect(Collectors.toList());

        assertThat(interceptorContexts.size(), is(2));
        assertThat(interceptorContexts.get(0).inputEnvelope(), is(envelope_1));
        assertThat(interceptorContexts.get(1).inputEnvelope(), is(envelope_2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldProcessEmptyStreamOfEventsAndReturnTheOriginalInterceptorContext() throws Exception {
        final JsonEnvelope envelope_1 = mock(JsonEnvelope.class);

        final Stream<JsonEnvelope> envelopeStream = Stream.empty();
        final InterceptorContext interceptorContext = interceptorContextWithInput(envelope_1, mock(InjectionPoint.class));

        when(eventBufferService.currentOrderedEventsWith(envelope_1)).thenReturn(envelopeStream);
        when(interceptorChain.processNext(any(Stream.class))).thenReturn(emptyList());

        final InterceptorContext resultContext = eventBufferInterceptor.process(interceptorContext, interceptorChain);
        assertThat(resultContext, is(interceptorContext));

        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);

        verify(interceptorChain).processNext(argumentCaptor.capture());

        final List<InterceptorContext> interceptorContexts = ((Stream<InterceptorContext>) argumentCaptor
                .getValue())
                .collect(Collectors.toList());

        assertThat(interceptorContexts.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnEventBufferPriority() throws Exception {
        assertThat(eventBufferInterceptor.priority(), is(EVENT_BUFFER_PRIORITY));
    }
}