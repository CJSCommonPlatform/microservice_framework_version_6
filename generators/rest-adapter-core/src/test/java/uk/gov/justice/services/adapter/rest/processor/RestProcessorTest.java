package uk.gov.justice.services.adapter.rest.processor;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.adapter.rest.parameter.ParameterType.BOOLEAN;
import static uk.gov.justice.services.adapter.rest.parameter.ParameterType.NUMERIC;
import static uk.gov.justice.services.adapter.rest.parameter.ParameterType.STRING;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
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

/**
 * Unit tests for the {@link RestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestProcessorTest {

    private static final Optional<JsonObject> NOT_USED_PAYLOAD = Optional.of(Json.createObjectBuilder().build());
    private static final Collection<Parameter> NOT_USED_PATH_PARAMS = emptyList();
    private static final HttpHeaders NOT_USED_HEADERS = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
    private static final String NOT_USED_ACTION = "actionABC";

    @Mock
    private Consumer<JsonEnvelope> consumer;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> function;

    private RestProcessor restProcessor;

    private Metadata metadata;

    @Before
    public void setup() {
        restProcessor = new RestProcessor(new RestEnvelopeBuilderFactory(), envelope -> new JsonObjectEnvelopeConverter().fromEnvelope(envelope).toString(), false);


    }

    @Test
    public void shouldReturn202ResponseOnAsyncProcessing() throws Exception {
        Response response = restProcessor.processAsynchronously(consumer, NOT_USED_ACTION, NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(202));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToConsumerOnAsyncProcessing() throws Exception {
        Optional<JsonObject> payload = Optional.of(Json.createObjectBuilder().add("key123", "value45678").build());

        restProcessor.processAsynchronously(consumer, NOT_USED_ACTION, payload, NOT_USED_HEADERS, asList(Parameter.valueOf("paramABC", "paramValueBCD", STRING)));

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("key123"), is("value45678"));
        assertThat(envelope.payloadAsJsonObject().getString("paramABC"), is("paramValueBCD"));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToConsumerOnAsyncProcessing() throws Exception {

        restProcessor.processAsynchronously(consumer, "some.action", NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is("some.action"));
    }

    @Test
    public void shouldReturn200ResponseOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(envelope().with(metadataWithDefaults()).build());
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void shouldReturn404ResponseOnSyncProcessingIfPayloadIsJsonValueNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(envelopeFrom(metadataWithDefaults(), JsonValue.NULL));
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void shouldReturn500ResponseOnSyncProcessingIfEnvelopeIsNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(null);
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(500));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToFunctionOnSyncProcessing() throws Exception {
        String action = "somecontext.somequery";
        restProcessor.processSynchronously(function, action, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(action));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToFunctionOnSyncProcessing() throws Exception {

        restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS,
                asList(Parameter.valueOf("param1", "paramValue345", STRING),
                        Parameter.valueOf("param2", "5555", NUMERIC),
                        Parameter.valueOf("param3", "true", BOOLEAN)
                ));

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("param1"), is("paramValue345"));
        assertThat(envelope.payloadAsJsonObject().getInt("param2"), is(5555));
        assertThat(envelope.payloadAsJsonObject().getBoolean("param3"), is(true));

    }

    @Test
    public void shouldReturnPayloadOfEnvelopeReturnedByFunction() {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelope().with(metadataWithDefaults()).withPayloadOf("value33", "key11").withPayloadOf("value55", "key22").build());

        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);
        String responseEntity = (String) response.getEntity();
        with(responseEntity)
                .assertThat("key11", equalTo("value33"))
                .assertThat("key22", equalTo("value55"));
    }

    @Test
    public void shouldReturnPayloadOnlyAndMetadataIdInHeader() {
        RestProcessor payLoadOnlyProcessor = new RestProcessor(new RestEnvelopeBuilderFactory(), envelope -> envelope.payload().toString(), true);

        final UUID metadataId = UUID.randomUUID();
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelope().with(metadataOf(metadataId, "name1")).build());

        Response response = payLoadOnlyProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        with((String) response.getEntity())
                .assertNotDefined(JsonEnvelope.METADATA);
        assertThat(response.getHeaderString(HeaderConstants.ID), equalTo(metadataId.toString()));
    }

}
