package uk.gov.justice.services.adapter.direct;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryViewDirectAdapterTest {

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @InjectMocks
    private QueryViewDirectAdapter directAdapter;

    @Test
    public void shouldPassEnvelopeToInterceptorChain() throws Exception {
        when(interceptorChainProcessor.process(any(InterceptorContext.class))).thenReturn(Optional.of(envelope().build()));

        final JsonEnvelope envelopePassedToAdapter = envelope().build();

        directAdapter.process(envelopePassedToAdapter);
        ArgumentCaptor<InterceptorContext> interceptorContext = ArgumentCaptor.forClass(InterceptorContext.class);

        verify(interceptorChainProcessor).process(interceptorContext.capture());

        assertThat(interceptorContext.getValue().inputEnvelope(), is(envelopePassedToAdapter));

    }
}