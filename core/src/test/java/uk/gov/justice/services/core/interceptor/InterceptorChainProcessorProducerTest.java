package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.dispatcher.Dispatcher;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.test.utils.common.MemberInjectionPoint;

import javax.inject.Inject;

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
    private Logger logger;

    @Mock
    private DispatcherCache dispatcherCache;

    @Mock
    private InterceptorCache interceptorCache;

    @InjectMocks
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Test
    public void shouldProduceProcessorThatDispatchesAnEnvelopeAndReturnsOutputEnvelope() throws Exception {
        final MemberInjectionPoint injectionPoint = injectionPointWith(AdapterAnnotation.class.getDeclaredField("processor"));

        when(dispatcherCache.dispatcherFor(injectionPoint)).thenReturn(dispatcher);

        final InterceptorChainProcessor result = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        assertThat(result, instanceOf(DefaultInterceptorChainProcessor.class));
    }

    @Adapter(EVENT_LISTENER)
    public static class AdapterAnnotation {

        @Inject
        InterceptorChainProcessor processor;

    }
}