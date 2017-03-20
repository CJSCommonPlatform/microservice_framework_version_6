package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultInterceptorChainProcessorTest {

    @Mock
    private InterceptorCache interceptorCache;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> dispatch;

    @Test
    public void shouldProcessInterceptorContext() throws Exception {
        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(inputEnvelope);

        when(interceptorCache.getInterceptors()).thenReturn(interceptors());
        when(dispatch.apply(inputEnvelope)).thenReturn(outputEnvelope);

        final DefaultInterceptorChainProcessor interceptorChainProcessor = new DefaultInterceptorChainProcessor(interceptorCache, dispatch);

        final Optional<JsonEnvelope> result = interceptorChainProcessor.process(interceptorContext);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(outputEnvelope));
    }

    @Test
    public void shouldProcessJsonEnvelope() throws Exception {
        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final JsonEnvelope outputEnvelope = mock(JsonEnvelope.class);

        when(interceptorCache.getInterceptors()).thenReturn(interceptors());
        when(dispatch.apply(inputEnvelope)).thenReturn(outputEnvelope);

        final DefaultInterceptorChainProcessor interceptorChainProcessor = new DefaultInterceptorChainProcessor(interceptorCache, dispatch);

        final Optional<JsonEnvelope> result = interceptorChainProcessor.process(inputEnvelope);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(outputEnvelope));
    }

    @Test
    public void shouldProcessesDispatcherThatReturnsNull() throws Exception {
        final JsonEnvelope inputEnvelope = mock(JsonEnvelope.class);
        final InterceptorContext interceptorContext = interceptorContextWithInput(inputEnvelope);

        when(interceptorCache.getInterceptors()).thenReturn(interceptors());
        when(dispatch.apply(inputEnvelope)).thenReturn(null);

        final DefaultInterceptorChainProcessor interceptorChainProcessor = new DefaultInterceptorChainProcessor(interceptorCache, dispatch);
        final Optional<JsonEnvelope> result = interceptorChainProcessor.process(inputEnvelope);

        assertThat(result, is(Optional.empty()));
    }

    private LinkedList<Interceptor> interceptors() {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.add(new TestInterceptor());
        return interceptors;
    }

    private static class TestInterceptor implements Interceptor {

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