package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;

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
    private Dispatcher dispatcher;

    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private Logger logger;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private InterceptorCache interceptorCache;

    @InjectMocks
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Test
    public void shouldProduceProcessorThatDispatchesAnEnvelopeAndReturnsOutputEnvelope() throws Exception {
        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final InterceptorChainProcessor result = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        assertThat(result, instanceOf(DefaultInterceptorChainProcessor.class));
    }
}