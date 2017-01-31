package uk.gov.justice.services.adapter.rest.processor;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.adapter.rest.processor.ResponseStrategyFactory.ACCEPTED_STATUS_WITH_NO_ENTITY;
import static uk.gov.justice.services.adapter.rest.processor.ResponseStrategyFactory.OK_STATUS_WITH_ENVELOPE_ENTITY;
import static uk.gov.justice.services.adapter.rest.processor.ResponseStrategyFactory.OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultResponseStrategyFactoryTest {

    private static final String UNKNOWN_STRATEGY = "UNKNOWN";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Function<JsonEnvelope, Response> function;

    @Spy
    private ResponseFactoryHelper responseFactoryHelper;

    @Spy
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @InjectMocks
    DefaultResponseStrategyFactory responseStrategyFactory;

    @Test
    public void shouldReturnOkStatusWithEnvelopeEntity() throws Exception {
        final UUID id = randomUUID();
        final String name = "Name";
        final String payloadValue = "payload";
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, name))
                .withPayloadOf("payload", payloadValue)
                .build();

        when(function.apply(jsonEnvelope)).thenReturn(status(OK).build());

        final Response response = responseStrategyFactory
                .strategyFor(OK_STATUS_WITH_ENVELOPE_ENTITY)
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

        final JsonObject entity = (JsonObject) response.getEntity();

        with(entity.toString())
                .assertEquals("$._metadata.id", id.toString())
                .assertEquals("$._metadata.name", name)
                .assertEquals("$.payload", payloadValue);
    }

    @Test
    public void shouldReturnOkStatusWithPayloadOfEnvelopeAsEntity() throws Exception {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload")
                .build();

        when(function.apply(jsonEnvelope)).thenReturn(status(OK).build());

        final Response response = responseStrategyFactory
                .strategyFor(OK_STATUS_WITH_ENVELOPE_PAYLOAD_ENTITY)
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
        assertThat(response.getHeaderString(ID), is(id.toString()));

        final JsonObject entity = (JsonObject) response.getEntity();
        assertThat(entity.toString(), is("{\"payload\":\"payload\"}"));
    }

    @Test
    public void shouldReturnAcceptedStatusWithNoEntity() throws Exception {
        final UUID id = randomUUID();
        final JsonEnvelope jsonEnvelope = envelope()
                .with(metadataOf(id, "Name"))
                .withPayloadOf("payload", "payload")
                .build();

        when(function.apply(jsonEnvelope)).thenReturn(status(ACCEPTED).build());

        final Response response = responseStrategyFactory
                .strategyFor(ACCEPTED_STATUS_WITH_NO_ENTITY)
                .responseFor("action.name", Optional.of(jsonEnvelope));

        assertThat(response.getStatus(), equalTo(ACCEPTED.getStatusCode()));

        final Object entity = response.getEntity();
        assertThat(entity, is(nullValue()));
    }

    @Test
    public void shouldThrowExceptionForUnknownStrategyType() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Strategy of type [UNKNOWN] is not a recognised strategy type");

        responseStrategyFactory.strategyFor(UNKNOWN_STRATEGY);
    }
}