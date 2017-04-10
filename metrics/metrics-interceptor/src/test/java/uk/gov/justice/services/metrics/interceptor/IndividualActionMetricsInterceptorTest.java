package uk.gov.justice.services.metrics.interceptor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;
import uk.gov.justice.services.core.interceptor.DefaultInterceptorContext;
import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndividualActionMetricsInterceptorTest {

    @Mock
    private MetricRegistry metricsRegistry;

    @Mock
    private InterceptorChain interceptorChain;

    @Mock
    private Timer timer;

    @Mock
    private Timer.Context timerContext;

    @Mock
    ServiceContextNameProvider serviceContextNameProvider;

    @InjectMocks
    private IndividualActionMetricsInterceptor interceptor;


    @Test
    public void shouldGetTimerFromRegistryByContextName() {

        when(serviceContextNameProvider.getServiceContextName()).thenReturn("someCtxName");
        when(metricsRegistry.timer("someCtxName.action.actionNameABC")).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        interceptor.process(interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("actionNameABC")).build()), interceptorChain);

        verify(metricsRegistry).timer("someCtxName.action.actionNameABC");
    }

    @Test
    public void shouldStartAndStopTimer() throws Exception {

        when(metricsRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        final InterceptorContext currentContext = interceptorContextWithInput(envelope().with(metadataWithDefaults()).build());

        interceptor.process(currentContext, interceptorChain);

        final InOrder inOrder = Mockito.inOrder(timer, interceptorChain, timerContext);
        inOrder.verify(timer).time();
        inOrder.verify(interceptorChain).processNext(currentContext);
        inOrder.verify(timerContext).stop();

    }

}
