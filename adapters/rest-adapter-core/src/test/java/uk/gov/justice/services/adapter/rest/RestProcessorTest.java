package uk.gov.justice.services.adapter.rest;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

/**
 * Unit tests for the {@link RestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestProcessorTest {

    private static final JsonObject NOT_USED_PAYLOAD = Json.createObjectBuilder().build();
    private static final HashMap<String, String> NOT_USED_PATH_PARAMS = new HashMap<>();
    private static final HttpHeaders NOT_USED_HEADERS;

    static {
        MultivaluedMap<String,String> headersMap = new MultivaluedMapImpl<>();
        headersMap.add("Content-Type", "application/vnd.context.commands.command+json");
        NOT_USED_HEADERS = new ResteasyHttpHeaders(headersMap);
    }

    @Mock
    private Consumer<JsonEnvelope> consumer;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> function;

    private RestProcessor restProcessor;

    @Before
    public void setup() {
        restProcessor = new RestProcessor();
        restProcessor.envelopeBuilderFactory = new RestEnvelopeBuilderFactory();
    }

    @Test
    public void shouldReturn202ResponseOnAsyncProcessing() throws Exception {
        Response response = restProcessor.processAsynchronously(consumer, NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(202));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToConsumerOnAsyncProcessing() throws Exception {
        JsonObject payload = Json.createObjectBuilder().add("key123", "value45678").build();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("paramABC", "paramValueBCD");

        restProcessor.processAsynchronously(consumer, payload, NOT_USED_HEADERS, pathParams);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope jsonEnvelope = envelopeCaptor.getValue();
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("key123"), is("value45678"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("paramABC"), is("paramValueBCD"));

    }

    @Test
    public void shouldPassEnvelopeWithMetadataToConsumerOnAsyncProcessing() throws Exception {
        JsonObject payload = Json.createObjectBuilder().add("key123", "value45678").build();

        restProcessor.processAsynchronously(consumer, NOT_USED_PAYLOAD,
                headersWith("Content-Type", "application/vnd.somecontext.commands.somecommand+json"), NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope jsonEnvelope = envelopeCaptor.getValue();
        assertThat(jsonEnvelope.metadata().name(), is("somecontext.commands.somecommand"));
    }

    @Test
    public void shouldReturn200ResponseOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelopeFrom(null, Json.createObjectBuilder().build()));
        Response response = restProcessor.processSynchronously(function, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void shouldReturn404ResponseOnSyncProcessingIfFunctionReturnsNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(null);
        Response response = restProcessor.processSynchronously(function, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToFunctionOnSyncProcessing() throws Exception {
        restProcessor.processSynchronously(function,
                headersWith("Accept", "application/vnd.somecontext.queries.somequery+json"), NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope jsonEnvelope = envelopeCaptor.getValue();
        assertThat(jsonEnvelope.metadata().name(), is("somecontext.queries.somequery"));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToFunctionOnSyncProcessing() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("param1", "paramValue345");

        restProcessor.processSynchronously(function, NOT_USED_HEADERS, pathParams);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope jsonEnvelope = envelopeCaptor.getValue();
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("param1"), is("paramValue345"));

    }

    @Test
    public void shouldReturnPayloadOfEnvelopeReturnedByFunction() {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelopeFrom(null, Json.createObjectBuilder().add("key11", "value33").add("key22", "value55").build()));

        Response response = restProcessor.processSynchronously(function, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);
        String responseEntity = (String) response.getEntity();
        with(responseEntity)
                .assertThat("key11", equalTo("value33"))
                .assertThat("key22", equalTo("value55"));
    }


    private HttpHeaders headersWith(String headerName, String headerValue) {
        MultivaluedMapImpl headersMap = new MultivaluedMapImpl();
        headersMap.add(headerName, headerValue);
        return new ResteasyHttpHeaders(headersMap);
    }


}
