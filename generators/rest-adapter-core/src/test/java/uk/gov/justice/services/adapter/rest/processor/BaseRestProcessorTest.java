package uk.gov.justice.services.adapter.rest.processor;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.rest.ParameterType.BOOLEAN;
import static uk.gov.justice.services.rest.ParameterType.NUMERIC;
import static uk.gov.justice.services.rest.ParameterType.STRING;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class BaseRestProcessorTest {

    private static final Optional<JsonObject> NOT_USED_PAYLOAD = Optional.of(Json.createObjectBuilder().build());
    private static final Collection<Parameter> NOT_USED_PATH_PARAMS = emptyList();
    private static final HttpHeaders NOT_USED_HEADERS = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
    private static final String NOT_USED_ACTION = "actionABC";

    @Mock
    private Logger logger;

    @Mock
    private Response response;

    @Mock
    private Consumer<JsonEnvelope> consumer;

    @Mock
    private Function<JsonEnvelope, Optional<JsonEnvelope>> function;

    private RestProcessor restProcessor;

    @Before
    public void setup() {
        restProcessor = new ImplOfBaseRestProcessor(new RestEnvelopeBuilderFactory(), logger, response);
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToConsumerOnAsyncProcessing() throws Exception {
        final Optional<JsonObject> payload = Optional.of(Json.createObjectBuilder().add("key123", "value45678").build());

        restProcessor.processAsynchronously(consumer, NOT_USED_ACTION, payload, NOT_USED_HEADERS, singletonList(Parameter.valueOf("paramABC", "paramValueBCD", STRING)));

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("key123"), is("value45678"));
        assertThat(envelope.payloadAsJsonObject().getString("paramABC"), is("paramValueBCD"));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToConsumerOnAsyncProcessing() throws Exception {
        restProcessor.processAsynchronously(consumer, "some.action", NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is("some.action"));
    }

    @Test
    public void shouldReturnOkResponseOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(envelope().with(metadataWithDefaults()).build()));
        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response, equalTo(response));
    }

    @Test
    public void shouldReturn404ResponseOnSyncProcessingIfPayloadIsJsonValueNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.of(envelopeFrom(metadataWithDefaults(), JsonValue.NULL)));
        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void shouldReturn500ResponseOnSyncProcessingIfEnvelopeIsNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.empty());
        final Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToFunctionOnSyncProcessing() throws Exception {
        final String action = "somecontext.somequery";
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.empty());

        restProcessor.processSynchronously(function, action, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(action));
    }

    @Test
    public void shouldPassEnvelopeWithParametersToFunctionOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.empty());

        restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS,
                asList(Parameter.valueOf("param1", "paramValue345", STRING),
                        Parameter.valueOf("param2", "5555", NUMERIC),
                        Parameter.valueOf("param3", "true", BOOLEAN)
                ));

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("param1"), is("paramValue345"));
        assertThat(envelope.payloadAsJsonObject().getInt("param2"), is(5555));
        assertThat(envelope.payloadAsJsonObject().getBoolean("param3"), is(true));

    }

    @Test
    public void shouldPassEnvelopeWithPayloadToFunctionOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(Optional.empty());
        final Optional<JsonObject> payload = Optional.of(Json.createObjectBuilder().add("key123", "value45678").build());

        restProcessor.processSynchronously(function, NOT_USED_ACTION, payload, NOT_USED_HEADERS, singletonList(Parameter.valueOf("paramABC", "paramValueBCD", STRING)));

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(function).apply(envelopeCaptor.capture());

        final JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("key123"), is("value45678"));
        assertThat(envelope.payloadAsJsonObject().getString("paramABC"), is("paramValueBCD"));
    }

    private static class ImplOfBaseRestProcessor extends BaseRestProcessor {
        private final Response response;

        ImplOfBaseRestProcessor(final RestEnvelopeBuilderFactory envelopeBuilderFactory, final Logger logger, final Response response) {
            super(envelopeBuilderFactory, logger);
            this.response = response;
        }

        @Override
        protected Response okResponseFrom(final JsonEnvelope envelope) {
            return response;
        }
    }
}