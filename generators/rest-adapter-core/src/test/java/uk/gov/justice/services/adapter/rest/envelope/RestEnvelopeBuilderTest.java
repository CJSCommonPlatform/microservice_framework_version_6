package uk.gov.justice.services.adapter.rest.envelope;

import static java.lang.String.format;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.rest.ParameterType.BOOLEAN;
import static uk.gov.justice.services.rest.ParameterType.NUMERIC;
import static uk.gov.justice.services.rest.ParameterType.STRING;
import static uk.gov.justice.services.common.http.HeaderConstants.CLIENT_CORRELATION_ID;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CLIENT_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.STREAM_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.VERSION;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonObject;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.adapter.rest.parameter.Parameter;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RestEnvelopeBuilder} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestEnvelopeBuilderTest {

    private static final UUID UUID_ID = UUID.randomUUID();
    private static final UUID UUID_CLIENT_CORRELATION_ID = UUID.randomUUID();
    private static final UUID UUID_USER_ID = UUID.randomUUID();
    private static final UUID UUID_SESSION_ID = UUID.randomUUID();
    private static final String PAYLOAD_NAME = "action.name";
    private static final UUID PAYLOAD_METADATA_ID = UUID.randomUUID();
    private static final UUID UUID_STREAM_ID = UUID.randomUUID();
    private static final Long VERSION_VALUE = 1L;
    private static final String EXPECTED_MESAGE_TEMPLATE = "The metadata of payload and the headers both have %s set and the values are not equal: payload = %s, headers = %s";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBuildEnvelopeWithUUID() throws Exception {

        final UUID uuid = UUID.randomUUID();
        final JsonEnvelope envelope = new RestEnvelopeBuilder(uuid).withAction("a").build();
        assertThat(envelope.metadata().id(), equalTo(uuid));
    }

    @Test
    public void shouldBuildEmptyEnvelopeWithNameBasedOnAction() throws Exception {

        final JsonEnvelope envelope = new RestEnvelopeBuilder(UUID_ID).withAction("blah").build();
        assertThat(envelope.metadata().name(), equalTo("blah"));
    }

    @Test
    public void shouldAddInitialPayload() throws Exception {

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add("test", "value")
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction().withInitialPayload(initialPayload).build();

        assertThat(envelope.payloadAsJsonObject(), equalTo(initialPayload.get()));
    }

    @Test
    public void shouldAddPathParams() throws Exception {

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(
                        Optional.of(createObjectBuilder()
                                .add("test", "value")
                                .build()))
                .build();

        final JsonObject expectedPayload = createObjectBuilder()
                .add("test", "value")
                .build();

        assertThat(envelope.payloadAsJsonObject(), equalTo(expectedPayload));
    }

    @Test
    public void shouldAddStringParam() {

        final RestEnvelopeBuilder builder = builderWithDefaultAction().withParams(ImmutableList.of(Parameter.valueOf("test2", "value2", STRING)));
        final JsonEnvelope envelope = builder.build();
        assertThat(envelope.payloadAsJsonObject().getString("test2"), is("value2"));
    }

    @Test
    public void shouldAddNumericParams() {

        final RestEnvelopeBuilder builder = builderWithDefaultAction().withParams(ImmutableList.of(Parameter.valueOf("param1", "3", NUMERIC)));
        final JsonEnvelope envelope = builder.build();
        assertThat(envelope.payloadAsJsonObject().getInt("param1"), is(3));
    }

    @Test
    public void shouldAddBooleanParams() {

        final RestEnvelopeBuilder builder = builderWithDefaultAction().withParams(ImmutableList.of(Parameter.valueOf("param1", "true", BOOLEAN)));
        final JsonEnvelope envelope = builder.build();
        assertThat(envelope.payloadAsJsonObject().getBoolean("param1"), is(true));
    }

    @Test
    public void shouldSetClientCorrelationId() throws Exception {

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString())))
                .build();

        assertThat(envelope.metadata().clientCorrelationId(), equalTo(Optional.of(UUID_CLIENT_CORRELATION_ID.toString())));
    }

    @Test
    public void shouldSetUserId() throws Exception {

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.USER_ID, UUID_USER_ID.toString())))
                .build();

        assertThat(envelope.metadata().userId().isPresent(), is(true));
        assertThat(envelope.metadata().userId().get(), equalTo(UUID_USER_ID.toString()));
    }

    @Test
    public void shouldSetSessionId() throws Exception {

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())))
                .build();

        assertThat(envelope.metadata().sessionId().isPresent(), is(true));
        assertThat(envelope.metadata().sessionId().get(), equalTo(UUID_SESSION_ID.toString()));
    }

    @Test
    public void shouldRemoveMetadataFromPayloadAndAddToMetadataOfEnvelope() throws Exception {

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, PAYLOAD_METADATA_ID.toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CONTEXT, createObjectBuilder()
                                .add(SESSION_ID, UUID_SESSION_ID.toString())
                                .add(USER_ID, UUID_USER_ID.toString())
                        )
                        .add(CORRELATION, createObjectBuilder()
                                .add(CLIENT_ID, UUID_CLIENT_CORRELATION_ID.toString()))
                        .add(STREAM, createObjectBuilder()
                                .add(STREAM_ID, UUID_STREAM_ID.toString())
                                .add(VERSION, VERSION_VALUE))
                )
                .add("test", "value")
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .build();

        final JsonObject payload = envelope.payloadAsJsonObject();
        final Metadata metadata = envelope.metadata();

        assertThat(metadata.id(), is(PAYLOAD_METADATA_ID));
        assertThat(metadata.name(), is(PAYLOAD_NAME));
        assertThat(metadata.sessionId(), is(Optional.of(UUID_SESSION_ID.toString())));
        assertThat(metadata.userId(), is(Optional.of(UUID_USER_ID.toString())));
        assertThat(metadata.clientCorrelationId(), is(Optional.of(UUID_CLIENT_CORRELATION_ID.toString())));
        assertThat(metadata.streamId(), is(Optional.of(UUID_STREAM_ID)));
        assertThat(metadata.version(), is(Optional.of(VERSION_VALUE)));

        assertThat(getString(payload, METADATA, ID), is(Optional.empty()));
        assertThat(getString(payload, METADATA, NAME), is(Optional.empty()));
        assertThat(getJsonObject(payload, METADATA, CONTEXT), is(Optional.empty()));
        assertThat(getJsonObject(payload, METADATA, CORRELATION), is(Optional.empty()));
        assertThat(getJsonObject(payload, METADATA, STREAM), is(Optional.empty()));

        assertThat(getString(payload, "test"), is(Optional.of("value")));
    }

    @Test
    public void shouldMergeHeaderWithPayloadMetadata() throws Exception {

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, PAYLOAD_METADATA_ID.toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(STREAM, createObjectBuilder()
                                .add(STREAM_ID, UUID_STREAM_ID.toString())
                                .add(VERSION, VERSION_VALUE))
                )
                .add("test", "value")
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(
                                CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString(),
                                HeaderConstants.USER_ID, UUID_USER_ID.toString(),
                                HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())))
                .build();

        final Metadata metadata = envelope.metadata();

        assertThat(metadata.id(), is(PAYLOAD_METADATA_ID));
        assertThat(metadata.name(), is(PAYLOAD_NAME));
        assertThat(metadata.sessionId(), is(Optional.of(UUID_SESSION_ID.toString())));
        assertThat(metadata.userId(), is(Optional.of(UUID_USER_ID.toString())));
        assertThat(metadata.clientCorrelationId(), is(Optional.of(UUID_CLIENT_CORRELATION_ID.toString())));
        assertThat(metadata.streamId(), is(Optional.of(UUID_STREAM_ID)));
        assertThat(metadata.version(), is(Optional.of(VERSION_VALUE)));
    }

    @Test
    public void shouldFailIfUserIdIsSetInBothHeaderAndPayloadAndAreNotEqual() throws Exception {

        final String payloadUserId = UUID_USER_ID.toString();
        final String headerUserId = UUID.randomUUID().toString();

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage(format(EXPECTED_MESAGE_TEMPLATE, "User Id", payloadUserId, headerUserId));

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CONTEXT, createObjectBuilder()
                                .add(USER_ID, payloadUserId)
                        ))
                .build());

        builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.USER_ID, headerUserId)))
                .build();
    }

    @Test
    public void shouldNotFailIfUserIdIsSetInBothHeaderAndPayloadAndAreEqual() throws Exception {

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CONTEXT, createObjectBuilder()
                                .add(USER_ID, UUID_USER_ID.toString())
                        ))
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.USER_ID, UUID_USER_ID.toString())))
                .build();

        final Metadata metadata = envelope.metadata();
        assertThat(metadata.userId(), is(Optional.of(UUID_USER_ID.toString())));
    }

    @Test
    public void shouldFailIfSessionIdIsSetInBothHeaderAndPayloadAndAreNoEqual() throws Exception {

        final String payloadSessionId = UUID_SESSION_ID.toString();
        final String headerSessionId = UUID.randomUUID().toString();

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage(format(EXPECTED_MESAGE_TEMPLATE, "Session Id", payloadSessionId, headerSessionId));

        final Optional<JsonObject>
                initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CONTEXT, createObjectBuilder()
                                .add(SESSION_ID, payloadSessionId)
                        ))
                .build());

        builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.SESSION_ID, headerSessionId)))
                .build();
    }

    @Test
    public void shouldNotFailIfSessionIdIsSetInBothHeaderAndPayloadAndAreEqual() throws Exception {

        final Optional<JsonObject>
                initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CONTEXT, createObjectBuilder()
                                .add(SESSION_ID, UUID_SESSION_ID.toString())
                        ))
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(HeaderConstants.SESSION_ID, UUID_SESSION_ID.toString())))
                .build();

        final Metadata metadata = envelope.metadata();
        assertThat(metadata.sessionId(), is(Optional.of(UUID_SESSION_ID.toString())));
    }

    @Test
    public void shouldFailIfClientCorrelationIdIsSetInBothHeaderAndPayloadAndAreNotEqual() throws Exception {

        final String payloadClientId = UUID_CLIENT_CORRELATION_ID.toString();
        final String headerClientId = UUID.randomUUID().toString();

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage(format(EXPECTED_MESAGE_TEMPLATE, "Client Correlation Id", payloadClientId, headerClientId));

        final Optional<JsonObject>
                initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CORRELATION, createObjectBuilder()
                                .add(CLIENT_ID, payloadClientId)
                        ))
                .build());

        builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(CLIENT_CORRELATION_ID, headerClientId)))
                .build();
    }

    @Test
    public void shouldNotFailIfClientCorrelationIdIsSetInBothHeaderAndPayloadAndAreEqual() throws Exception {

        final Optional<JsonObject>
                initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, UUID.randomUUID().toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(CORRELATION, createObjectBuilder()
                                .add(CLIENT_ID, UUID_CLIENT_CORRELATION_ID.toString())
                        ))
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .withHeaders(
                        httpHeadersOf(ImmutableMap.of(CLIENT_CORRELATION_ID, UUID_CLIENT_CORRELATION_ID.toString())))
                .build();

        final Metadata metadata = envelope.metadata();
        assertThat(metadata.clientCorrelationId(), is(Optional.of(UUID_CLIENT_CORRELATION_ID.toString())));
    }

    @Test
    public void shouldPassNonFrameworkMetadataThroughIntoJsonEnvelope() throws Exception {

        final String external_1 = "External_1";
        final String external_2 = "External_2";
        final String value_1 = "external value 1";
        final String value_2 = "external value 2";

        final Optional<JsonObject> initialPayload = Optional.of(createObjectBuilder()
                .add(METADATA, createObjectBuilder()
                        .add(ID, PAYLOAD_METADATA_ID.toString())
                        .add(NAME, PAYLOAD_NAME)
                        .add(STREAM, createObjectBuilder()
                                .add(STREAM_ID, UUID_STREAM_ID.toString()))
                        .add(external_1, value_1)
                        .add(external_2, value_2)
                )
                .add("test", "value")
                .build());

        final JsonEnvelope envelope = builderWithDefaultAction()
                .withInitialPayload(initialPayload)
                .build();

        final Metadata metadata = envelope.metadata();
        final JsonObject metadataJson = metadata.asJsonObject();

        assertThat(metadataJson.getString(external_1), is(value_1));
        assertThat(metadataJson.getString(external_2), is(value_2));

        assertThat(metadata.id(), is(PAYLOAD_METADATA_ID));
        assertThat(metadata.name(), is(PAYLOAD_NAME));
        assertThat(metadata.streamId(), is(Optional.of(UUID_STREAM_ID)));
    }

    private RestEnvelopeBuilder builderWithDefaultAction() {
        return new RestEnvelopeBuilder(UUID_ID).withAction("abc");
    }

    private HttpHeaders httpHeadersOf(final Map<String, String> headersMap) {
        return new ResteasyHttpHeaders(new MultivaluedHashMap<>(headersMap));
    }
}
