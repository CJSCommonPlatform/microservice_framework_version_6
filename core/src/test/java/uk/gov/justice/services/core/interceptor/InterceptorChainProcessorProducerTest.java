package uk.gov.justice.services.core.interceptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.LinkedList;

import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainProcessorProducerTest {

    @Mock
    Logger logger;

    @Mock
    DispatcherCache dispatcherCache;

    @Mock
    InterceptorCache interceptorCache;

    @InjectMocks
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Test
    public void shouldProduceProcessorThatProcessesAJsonEnvelope() throws Exception {

        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final Dispatcher dispatcher = mock(Dispatcher.class);
        final InjectionPoint injectionPoint = mock(InjectionPoint.class);
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(new TestInterceptor());

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);
        when(interceptorCache.getInterceptors()).thenReturn(interceptors);

        interceptorChainProcessorProducer.produceProcessor(injectionPoint).process(envelope);

        verify(dispatcher).dispatch(envelope);
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