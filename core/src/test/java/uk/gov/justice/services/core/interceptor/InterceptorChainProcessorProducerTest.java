package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.LinkedList;
import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainProcessorProducerTest {

    @InjectMocks
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Mock
    private JsonEnvelope inputEnvelope;

    @Mock
    private Dispatcher dispatcher;

    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private Logger logger;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private InterceptorCache interceptorCache;

    @Test
    public void shouldProduceProcessorThatDispatchesAnEnvelopeAndReturnsOutputEnvelope() throws Exception {

        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(interceptorCache.getInterceptors()).thenReturn(interceptors());
        when(dispatcher.dispatch(inputEnvelope)).thenReturn(outputEnvelope);

        final Optional<JsonEnvelope> result = interceptorChainProcessorProducer.produceProcessor(injectionPoint).process(inputEnvelope);

        assertThat(result.get(), is(outputEnvelope));
    }

    @Test
    public void shouldProduceProcessorThatProcessesAJsonEnvelopeAndReturnsNull() throws Exception {

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(interceptorCache.getInterceptors()).thenReturn(interceptors());
        when(dispatcher.dispatch(inputEnvelope)).thenReturn(null);

        final Optional<JsonEnvelope> result = interceptorChainProcessorProducer.produceProcessor(injectionPoint).process(inputEnvelope);

        assertThat(result, is(Optional.empty()));
    }

    private LinkedList<Interceptor> interceptors() {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(new TestInterceptor());
        return interceptors;
    }

    public static class TestInterceptor implements Interceptor {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext, final InterceptorChain interceptorChain) {
            return interceptorChain.processNext(interceptorContext);
        }

        @Override
        public int priority() {
            return 1000;
        }
    }
}