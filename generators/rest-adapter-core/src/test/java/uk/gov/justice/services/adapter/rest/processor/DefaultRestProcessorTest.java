package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRestProcessorTest {

    private static final Collection<Parameter> PATH_PARAMS = emptyList();
    private static final HttpHeaders HEADERS = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
    private static final String ACTION = "actionABC";

    @Mock
    private Function<JsonEnvelope, Optional<JsonEnvelope>> interceptorChain;

    @Mock
    private JsonObject payload;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Response response;

    @Mock
    private ResponseStrategy responseStrategy;

    @Mock
    private Logger logger;

    @Spy
    private RestEnvelopeBuilderFactory restEnvelopeBuilderFactory;

    @InjectMocks
    private DefaultRestProcessor restProcessor;

    @Test
    public void shouldPassEnvelopeWithPayloadToInterceptorChain() throws Exception {
        restProcessor.process(responseStrategy, interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(interceptorChain).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(ACTION));
        assertThat(envelope.payloadAsJsonObject(), is(payload));
    }

    @Test
    public void shouldPassEnvelopeWithEmptyPayloadToInterceptorChain() throws Exception {
        restProcessor.process(responseStrategy, interceptorChain, ACTION, HEADERS, PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(interceptorChain).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(ACTION));
        assertThat(envelope.payloadAsJsonObject().toString(), is("{}"));
    }

    @Test
    public void shouldReturnResponseFromResponseFactoryForCallWithPayload() throws Exception {
        when(interceptorChain.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(jsonEnvelope));
        when(responseStrategy.responseFor(ACTION, Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessor.process(responseStrategy, interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        verify(responseStrategy).responseFor(ACTION, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }

    @Test
    public void shouldReturnResponseFromResponseFactoryForCallWithoutPayload() throws Exception {
        when(interceptorChain.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(jsonEnvelope));
        when(responseStrategy.responseFor(ACTION, Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessor.process(responseStrategy, interceptorChain, ACTION, HEADERS, PATH_PARAMS);

        verify(responseStrategy).responseFor(ACTION, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }
}