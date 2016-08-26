package uk.gov.justice.services.core.eventfilter;



import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.interceptor.InterceptorChain;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventFilterInterceptorTest {

    @Mock
    private EventFilter eventFilter;

    @InjectMocks
    private EventFilterInterceptor eventFilterInterceptor;

    @Mock
    private Function<JsonEnvelope,JsonEnvelope> dispatcher;


    @Mock
    private InterceptorChain interceptorChain;


    @Test
    public void shouldProcessNextInChainIfFilterAcceptsMessage() throws Exception {

        when(eventFilter.accepts("nameABC")).thenReturn(true);

        final InterceptorContext currentContext = interceptorContextWithInput(
                envelope().with(metadataWithRandomUUID("nameABC")).build(), null);
        final InterceptorContext nextInChain = interceptorContextWithInput(null, null);
        when(interceptorChain.processNext(currentContext)).thenReturn(nextInChain);

        assertThat(eventFilterInterceptor.process(currentContext, interceptorChain), is(nextInChain));

    }

    @Test
    public void shouldReturnInterceptorContextIfFilterRejectsMessage() throws Exception {

        when(eventFilter.accepts("nameBCD")).thenReturn(false);

        final InterceptorContext currentContext = interceptorContextWithInput(envelope().with(metadataWithRandomUUID("nameBCD")).build(), null);
        eventFilterInterceptor.process(currentContext, interceptorChain);

        assertThat(eventFilterInterceptor.process(currentContext, interceptorChain), is(currentContext));
    }

}