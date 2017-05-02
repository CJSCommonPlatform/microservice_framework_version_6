package uk.gov.justice.services.core.interceptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.MemberInjectionPoint.injectionPointWith;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.MemberInjectionPoint;
import uk.gov.justice.services.test.utils.common.envelope.EnvelopeRecordingInterceptor;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainProcessorProducerTest {

    private static final String ACTION_NAME = "abc";
    @Mock
    private Logger logger;

    @Mock
    private InterceptorCache interceptorCache;

    @InjectMocks
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    private EnvelopeRecordingInterceptor envelopeRecordingInterceptor = new EnvelopeRecordingInterceptor();
    private EnvelopeRecordingHandler envelopeRecordingHandler = new EnvelopeRecordingHandler();

    @Before
    public void setUp() throws Exception {
        interceptorChainProcessorProducer.dispatcherCache = new DispatcherCache();
        envelopeRecordingInterceptor.reset();
    }

    @Test
    public void shouldProduceProcessorThatDispatchesEnvelope_FromAdapter() throws Exception {
        when(interceptorCache.getInterceptors("EVENT_LISTENER")).thenReturn(envelopeRecordingInterceptor());

        final MemberInjectionPoint injectionPoint = injectionPointWith(EventListenerAdapter.class.getDeclaredField("processor"));
        interceptorChainProcessorProducer.dispatcherCache.dispatcherFor(injectionPoint).register(envelopeRecordingHandler);
        final InterceptorChainProcessor processor = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        final JsonEnvelope dispatchedEnvelope = envelope().with(metadataWithRandomUUID(ACTION_NAME)).build();

        processor.process(interceptorContextWithInput(dispatchedEnvelope));

        assertThat(envelopeRecordingInterceptor.firstRecordedEnvelope(), is(dispatchedEnvelope));
        assertThat(envelopeRecordingHandler.firstRecordedEnvelope(), is(dispatchedEnvelope));
    }

    @Test
    public void shouldProduceProcessorThatDispatchesEnvelope_FromDirectAdapter() throws Exception {
        when(interceptorCache.getInterceptors("QUERY_API")).thenReturn(envelopeRecordingInterceptor());

        final MemberInjectionPoint injectionPoint = injectionPointWith(QueryApiDirectAdapter.class.getDeclaredField("processor"));
        interceptorChainProcessorProducer.dispatcherCache.dispatcherFor(injectionPoint).register(envelopeRecordingHandler);
        final InterceptorChainProcessor processor = interceptorChainProcessorProducer.produceProcessor(injectionPoint);

        final JsonEnvelope dispatchedEnvelope = envelope().with(metadataWithRandomUUID(ACTION_NAME)).build();

        processor.process(interceptorContextWithInput(dispatchedEnvelope));

        assertThat(envelopeRecordingInterceptor.firstRecordedEnvelope(), is(dispatchedEnvelope));
        assertThat(envelopeRecordingHandler.firstRecordedEnvelope(), is(dispatchedEnvelope));
    }

    @Adapter(EVENT_LISTENER)
    public static class EventListenerAdapter {

        @Inject
        InterceptorChainProcessor processor;

    }

    @DirectAdapter(value = QUERY_API)
    public static class QueryApiDirectAdapter {

        @Inject
        InterceptorChainProcessor processor;

    }

    private LinkedList<Interceptor> envelopeRecordingInterceptor() {
        final LinkedList<Interceptor> interceptors = new LinkedList<>();

        interceptors.add(envelopeRecordingInterceptor);
        return interceptors;
    }

    public static class EnvelopeRecordingHandler extends TestEnvelopeRecorder {

        @Handles(ACTION_NAME)
        public void handles(final JsonEnvelope envelope) {
            record(envelope);

        }

    }


}