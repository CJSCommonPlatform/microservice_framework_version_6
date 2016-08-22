package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainTest {

    private InterceptorChain interceptorChain;

    @Before
    public void setup() throws Exception {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();
        interceptors.offer(new TestInterceptor());

        interceptorChain = new InterceptorChain(interceptors, new TestTarget());
    }

    @Test
    public void shouldProcessNextInterceptorInTheQueue() throws Exception {

        final InterceptorContext interceptorContext = mock(InterceptorContext.class);

        final InterceptorContext result = interceptorChain.processNext(interceptorContext);

        assertThat(result, is(interceptorContext));
    }

    @Test
    public void shouldSendEachInterceptorContextToTheNextInterceptorInTheQueue() throws Exception {

        final InterceptorContext interceptorContext_1 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_2 = mock(InterceptorContext.class);
        final InterceptorContext interceptorContext_3 = mock(InterceptorContext.class);

        final Stream<InterceptorContext> interceptorContexts = Stream.of(
                interceptorContext_1,
                interceptorContext_2,
                interceptorContext_3);

        final List<InterceptorContext> results = interceptorChain.processNext(interceptorContexts);

        assertThat(results.size(), is(3));
        assertThat(results, hasItems(
                interceptorContext_1,
                interceptorContext_2,
                interceptorContext_3));
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

    public static class TestTarget implements Target {

        @Override
        public InterceptorContext process(final InterceptorContext interceptorContext) {
            return interceptorContext;
        }
    }
}