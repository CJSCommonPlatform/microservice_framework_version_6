package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
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

    @Spy
    private RestEnvelopeBuilderFactory restEnvelopeBuilderFactory;

    @Mock
    private ResponseFactory responseFactory;

    @InjectMocks
    private DefaultRestProcessor restProcessor;

    @Test
    public void shouldPassEnvelopeWithPayloadToInterceptorChainForSynchronous() throws Exception {
        restProcessor.processSynchronously(interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(interceptorChain).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(ACTION));
        assertThat(envelope.payloadAsJsonObject(), is(payload));
    }

    @Test
    public void shouldPassEnvelopeWithEmptyPayloadToInterceptorChainForSynchronous() throws Exception {
        restProcessor.processSynchronously(interceptorChain, ACTION, HEADERS, PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(interceptorChain).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(ACTION));
        assertThat(envelope.payloadAsJsonObject().toString(), is("{}"));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToInterceptorChainForAsynchronousCall() throws Exception {
        restProcessor.processAsynchronously(interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(interceptorChain).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(ACTION));
        assertThat(envelope.payloadAsJsonObject(), is(payload));
    }

    @Test
    public void shouldReturnAcceptedResponseForAsynchronousCall() throws Exception {
        final Response result = restProcessor.processAsynchronously(interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        assertThat(result.getStatus(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturnResponseFromResponseFactoryForSynchronousCallWithPayload() throws Exception {
        when(interceptorChain.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(jsonEnvelope));
        when(responseFactory.responseFor(ACTION, Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessor.processSynchronously(interceptorChain, ACTION, Optional.of(payload), HEADERS, PATH_PARAMS);

        verify(responseFactory).responseFor(ACTION, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }

    @Test
    public void shouldReturnResponseFromResponseFactoryForSynchronousCallWithoutPayload() throws Exception {
        when(interceptorChain.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(jsonEnvelope));
        when(responseFactory.responseFor(ACTION, Optional.of(jsonEnvelope))).thenReturn(response);

        final Response result = restProcessor.processSynchronously(interceptorChain, ACTION, HEADERS, PATH_PARAMS);

        verify(responseFactory).responseFor(ACTION, Optional.of(jsonEnvelope));
        assertThat(result, equalTo(response));
    }
}